
let privateKey = "";
let publicKey = ""



function ab2str(buf) {
    
    return String.fromCharCode.apply(null, new Uint8Array(buf));
  }
  
  /*
  Export the given key and write it into the "exported-key" space.
  */
  async function exportPrivateCryptoKey(key) {
    const exported = await window.crypto.subtle.exportKey(
      "pkcs8",
      key
    );
    const exportedAsString = ab2str(exported);
    const exportedAsBase64 = window.btoa(exportedAsString);
    const pemExported = `-----BEGIN PRIVATE KEY-----\n${exportedAsBase64}\n-----END PRIVATE KEY-----`;
  
    
    privateKey = pemExported
  }

  async function exportPublicCryptoKey(key) {
    const exported = await window.crypto.subtle.exportKey(
      "spki",
      key
    );
    const exportedAsString = ab2str(exported);
    const exportedAsBase64 = window.btoa(exportedAsString);
    const pemExported = `-----BEGIN PUBLIC KEY-----\n${exportedAsBase64}\n-----END PUBLIC KEY-----`;
  
    publicKey = pemExported
  }
  
  /*
  Generate a sign/verify key pair,
  then set up an event listener on the "Export" button.
  */
  let keypair = window.crypto.subtle.generateKey(
    {
      name: "RSA-OAEP",
      // Consider using a 4096-bit key for systems that require long-term security
      modulusLength: 2048,
      publicExponent: new Uint8Array([1, 0, 1]),
      hash: "SHA-256",
    },
    true,
    ["encrypt", "decrypt"]
  )
  keypair.then((keyPair) => {
      exportPrivateCryptoKey(keyPair.privateKey)
      exportPublicCryptoKey(keyPair.publicKey)      
  });


  setTimeout(async () => {
  console.log(privateKey)
  console.log(publicKey);

    // keypair.then(kys => {
    //     encryptMessage(kys.publicKey, "asdsadhi").then(it => {
    //         console.log(`${new Uint8Array(it, 0, 5).toString()}`);

    //         decryptMessage(kys.privateKey, it)
               
    //     })
    // })

    const pubkey = await importPublicKey(publicKey)
    console.log(pubkey);
    const cipher = await encryptMessage(pubkey, ":hi")
    keypair.then(k => {
        decryptMessage(k.privateKey, cipher)
    })


    

      
  }, 1000);

  const encryptMessage = async (publicKey, message) => {
    const cipher = await window.crypto.subtle.encrypt(
      {
        name: "RSA-OAEP"
      },
      publicKey,
      new TextEncoder().encode(message)
    );
    // let buffer = new Uint8Array(cipher, 0, 5);
    return cipher;

  }

  const decryptMessage = async(privateKey, ciphertext) => {
      
    let decrypted = await window.crypto.subtle.decrypt(
        {
          name: "RSA-OAEP"
        },
        privateKey,
        ciphertext
      );
  
      let dec = new TextDecoder();
      
      console.log(dec.decode(decrypted))

      return dec.decode(decrypted)
  }

  const importPublicKey = async (pem) => {
      // fetch the part of the PEM string between header and footer
    const pemHeader = "-----BEGIN PUBLIC KEY-----";
    const pemFooter = "-----END PUBLIC KEY-----";
    const pemContents = pem.substring(pemHeader.length, pem.length - pemFooter.length);
    // base64 decode the string to get the binary data
    const binaryDerString = window.atob(pemContents);
    // convert from a binary string to an ArrayBuffer
    const binaryDer = str2ab(binaryDerString);

    return await window.crypto.subtle.importKey(
        "spki",
        binaryDer,
        {
        name: "RSA-OAEP",
        hash: "SHA-256"
        },
        true,
        ["encrypt"]
    );
  }

  function str2ab(str) {
    const buf = new ArrayBuffer(str.length);
    const bufView = new Uint8Array(buf);
    for (let i = 0, strLen = str.length; i < strLen; i++) {
      bufView[i] = str.charCodeAt(i);
    }
    return buf;
  }
  