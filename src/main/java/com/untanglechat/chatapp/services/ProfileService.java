package com.untanglechat.chatapp.services;

import java.time.Instant;
import java.util.Arrays;

import com.untanglechat.chatapp.dto.ProfileDTO;
import com.untanglechat.chatapp.dto.request.RegistrationOTPRequest;
import com.untanglechat.chatapp.dto.request.RsaTokenUpdateRequest;
import com.untanglechat.chatapp.dto.response.FluxResponse;
import com.untanglechat.chatapp.exceptions.InvalidOTPException;
import com.untanglechat.chatapp.exceptions.NoUserFoundException;
import com.untanglechat.chatapp.exceptions.UnAcceptableFormDataException;
import com.untanglechat.chatapp.exceptions.UsernameAlreadyExists;
import com.untanglechat.chatapp.models.Profile;
import com.untanglechat.chatapp.models.RegisterProfileOTP;
import com.untanglechat.chatapp.properties.S3ClientConfigurarionProperties;
import com.untanglechat.chatapp.repository.ProfileRepository;
import com.untanglechat.chatapp.repository.RegisterProfileOTPRepository;
import com.untanglechat.chatapp.security.UserPrincipal;
import com.untanglechat.chatapp.services.aws.AmazonAWSS3Service;
import com.untanglechat.chatapp.services.sms.SMSRequest;
import com.untanglechat.chatapp.services.sms.SmsService;
import com.untanglechat.chatapp.util.UtilityService;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ProfileService implements ReactiveUserDetailsService{

    // In minutes
    private static int REGISTRATION_OTP_TIME_LIMIT_MINUTES = 1;
    private static int REGISTRATION_OTP_LENGTH = 6;

    private final ProfileRepository profileRepository;
    private final RegisterProfileOTPRepository registerProfileOTPRepository;
    
    private final PasswordEncoder passwordEncoder;

    /**
     * Dependent services
     */
    private final S3ClientConfigurarionProperties s3config;
    private final AmazonAWSS3Service amazonAWSS3Service;
    private final SmsService smsService;

    private final UtilityService utilityService;

    @Autowired
    protected ProfileService(
        // REPOS
        final ProfileRepository profileRepository,
        final RegisterProfileOTPRepository registerProfileOTPRepository,
        // Services
        final AmazonAWSS3Service amazonAWSS3Service,
        @Qualifier("AmazonSMSService") final SmsService smsService,
        final UtilityService utilityService,
        // Configs
        final S3ClientConfigurarionProperties s3ClientConfigurarionProperties,
        // Other
        final PasswordEncoder passwordEncoder
    ) {
        // Repos
        this.profileRepository = profileRepository;
        this.registerProfileOTPRepository = registerProfileOTPRepository;
        
        // services
        this.amazonAWSS3Service = amazonAWSS3Service;
        this.smsService = smsService;
        this.utilityService = utilityService;

        // configs
        this.s3config = s3ClientConfigurarionProperties;

        // Other
        this.passwordEncoder = passwordEncoder;
    }

  

    @Override
    public Mono<UserDetails> findByUsername(final String username) {
        log.info("Fetching DB: "+ username);
        return profileRepository.findByUsername(username)
                    .map(profile -> new UserPrincipal(profile));
    }

    public Mono<Profile> getProfileByUsername(final String username) throws NoUserFoundException {
        return profileRepository.findByUsername(username).switchIfEmpty(Mono.error(() ->  new NoUserFoundException("User Does not exists")));
    }

    public Flux<Profile> getAllProfiles(){

        return profileRepository.findAll();
    }

    public Mono<Profile> registerProfile(final ProfileDTO profileDTO) throws UsernameAlreadyExists {

        // COPY properties from DTO to entity
        final Profile profile = new Profile();
        BeanUtils.copyProperties(profileDTO, profile);

        final boolean isProfileDataInvalid = utilityService.isProfileDTOAndModelValid(profileDTO, profile); 
        if(!isProfileDataInvalid) throw new UnAcceptableFormDataException("Invalid Form Data");

        // verify otp
        return verifyOTPOnRegistration(profileDTO.getUsername(), profileDTO.getVerificationOTP())
            .switchIfEmpty(Mono.error(new UnAcceptableFormDataException("Unable to Verify OTP")))    
            .doOnError(InvalidOTPException.class, (e)-> {
                throw new UnAcceptableFormDataException(e.getMessage());
            })
            .doOnNext(successStatus -> {
                if(successStatus == false) {
                    throw new UnAcceptableFormDataException("OTP Expired!!");
                }
            }).flatMap(it -> {
                profile.setRoles(Arrays.asList("ROLE_USER"));

                profile.setPassword(passwordEncoder.encode(profile.getPassword()));
        
                return this.profileRepository.existsByUsername(profile.getUsername())
                    .onErrorMap(e -> e)
                    .flatMap(usernameExists -> {
                        if(usernameExists) throw new UsernameAlreadyExists("Duplicate Username");
                        return profileRepository.save(profile);
                    });
            });        
    }

    /**
     * Verify if the Registration OTP has expired 
     * @param phoneNumber
     * @param otp
     * @return
     */
    private Mono<Boolean> verifyOTPOnRegistration(final String phoneNumber, final String otp) {
        return registerProfileOTPRepository.findById(phoneNumber)
            .switchIfEmpty(Mono.error(new InvalidOTPException("Please Resend OTP!!")))
            .map(registerProfileOTP -> {
                if(!registerProfileOTP.getOtp().equals(otp)) {
                    throw new InvalidOTPException("Wrong OTP!!");
                }
                
                if(Instant.now().isAfter(registerProfileOTP.getCreatedAt().plusMillis(1000 * 60 * REGISTRATION_OTP_TIME_LIMIT_MINUTES))) {
                    throw new InvalidOTPException("OTP Expired!!");
                } 

                return true;
            });
    }

    

    public Mono<Void> sendSMSOTPToRegister(final RegistrationOTPRequest registrationOTPRequest) {

        return registerProfileOTPRepository.deleteById(registrationOTPRequest.getPhoneNumber())
            .doOnSuccess(it -> {
                this.createStoreAndSendOTPOnRegistration(registrationOTPRequest).subscribe();
            });           
    }

    private Mono<Void> createStoreAndSendOTPOnRegistration(final RegistrationOTPRequest registrationOTPRequest) {
        // Create OTP
        final String OTP = utilityService.generateOTP(REGISTRATION_OTP_LENGTH);

        // Store OTP
        final RegisterProfileOTP registerProfileOTP = new RegisterProfileOTP();
        registerProfileOTP.setOtp(OTP);
        registerProfileOTP.setPhoneNumber(registrationOTPRequest.getPhoneNumber());
        registerProfileOTP.setCreatedAt(Instant.now());

        return registerProfileOTPRepository.save(registerProfileOTP)
            .doOnNext(it -> {
                // SEND OTP
                final SMSRequest smsRequest = new SMSRequest();
                smsRequest.setPhoneNumber(registerProfileOTP.getPhoneNumber());
                
                final String message = String.format("Your OTP is %s.", OTP);                    
                
                smsRequest.setMessage(message);

                smsService.sendSMS(Mono.just(smsRequest)).subscribe();
            }).then();

    }


    public Mono<Profile> updatePublicRSATokenForSubject(final String subject, final RsaTokenUpdateRequest tokenUpdateRequest) {
        return this.getProfileByUsername(subject)
            .flatMap(profile -> {
                profile.setPublicRSAKey(tokenUpdateRequest.getRsaPublicKey());
                return this.profileRepository.save(profile);                
            });
    }


    public Mono<String> changeProfilePicture(HttpHeaders headers, FilePart part) {

        final Claims claims = utilityService.extractAllClaimsFromRequest(headers);
        final String subject = claims.getSubject();

        return this.getProfileByUsername(subject)
            .flatMap(profile -> amazonAWSS3Service
                    .saveFile(headers, s3config.getBucket(), part)
                    .doOnNext((fileKey) -> {
                        profile.setProfilePictureS3ObjectId(fileKey);
                        this.profileRepository.save(profile).subscribe();
                    })
            );

    }

    public Mono<FluxResponse> downloadProfilePicture(final String filekey) {
        return amazonAWSS3Service.downloadFile(filekey);
    } 

}
