<template>
  <div class="container container-md d-flex flex-column">
    <div>
      <h3 class="text-center">Welcome to Estonian ID card mobile authentication demo website.</h3>
      <p>This website to demonstrates the viability of using your NFC-enabled ID-card and your smartphone to authenticate yourself.
        This is a proof of concept solution, so currently only authentication is supported. This solution was created for <a href="https://courses.cs.ut.ee/2021/tvp/">Software Project (Tarkvaraprojekt)</a> course in the University of Tartu
      in cooperation with <a href="https://github.com/martinpaljak/">Martin Paljak</a>.</p>
      <p>This solution is meant to be web-eid.js compatible, so this example website uses a <a href="https://github.com/TanelOrumaa/web-eid.js">fork of web-eid.js</a> which supports the Android authentication app.</p>
      <h2>Usage</h2>
      <p>To get started, download and install the authentication Android app from <a href="https://github.com/TanelOrumaa/Estonian-ID-card-mobile-authenticator-POC/releases">GitHub</a> (Android 8.0+ required).
        You can then click "Login" to authenticate yourself on this demo website with the app or if you are using a non-Android device, you can use both the app or the default web-eid.js option to login using the smartcard reader.
      </p>

      <p class="text-center">Read more from <a href="https://github.com/TanelOrumaa/Estonian-ID-card-mobile-authenticator-POC">here.</a></p>
    </div>
    <div class="justify-content-center d-flex">
      <div id="canvas"></div>
    </div>
    <div class="justify-content-center d-flex">
      <button type="button" class="btn loginButton btn-dark" v-on:click="authenticate">
        <div v-if="loading" class="d-flex justify-content-center">
          <div class="spinner-border text-light spinner-border-sm" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
        </div>
        <span v-else>Authenticate</span>
      </button>

    </div>
    <div class="btn-group-sm d-flex justify-content-center" v-if="!isAndroidDevice" role="group" aria-label="Basic radio toggle button group">
      <input type="radio" class="btn-check" name="btnradio" id="btnCardReader" autocomplete="off" v-on:click="useCardReader">
      <label class="btn btn-outline-secondary" for="btnCardReader">using ID-card reader</label>

      <input type="radio" class="btn-check" name="btnradio" id="btnApp" autocomplete="off" checked v-on:click="useApp">
      <label class="btn btn-outline-secondary" for="btnApp">using Android App</label>
    </div>

  </div>

</template>


<script>
import * as webeid from '../web-eid.js';
import router from "@/router";

export default {
  name: 'LoginComponent',
  data() {
    return {
      useAndroidApp: true,
      loading: false,
      challenge: "",
    }
  },
  methods: {
    useApp: function() {
      this.useAndroidApp = true;
    },

    useCardReader: function() {
      this.useAndroidApp = false;
    },



    authenticate: async function () {
      this.loading = true;

      const options = {
        getAuthChallengeUrl: window.location.origin + "/auth/challenge",
        postAuthTokenUrl: window.location.origin + "/auth/login",
        getAuthSuccessUrl: window.location.origin + "/auth/login",
        useAuthApp: this.useAndroidApp,
        headers: {
          "sessionId": this.$store.getters.getSessionId
        },
      };

      console.log(options);

      try {
        const response = await webeid.authenticate(options);
        console.log("Authentication successful! Response:", response);
        this.loading = false;
        this.$store.dispatch("setLoggedIn", true);
        await router.push("welcome");

      } catch (error) {
        console.log("Authentication failed! Error:", error);
        alert(error.message);
        this.loading = false;
        throw error;
      }
    }
  },
  computed: {
    isLoggedIn() {
      return this.$store.getAuthenticated;
    },
    loading() {
      return this.loading;
    },
    isAndroidDevice() {
      return this.$store.getters.getIsAndroid
    }
  },
  mounted() {
    const isAndroid = webeid.isAndroidDevice();
    this.$store.dispatch("setIsAndroid", isAndroid);
  }
}
</script>

<style scoped>
.container > div {
  margin-top: 2vh;
}
.loginButton {
  height: 4vh;
  width: 20vh;
  line-height: 3vh;
}

.loginButton > p {
  font-size: 3vh;
  text-align: center;
}

#canvas {
  height: 30vh;
  width: 30vh;
}
</style>
