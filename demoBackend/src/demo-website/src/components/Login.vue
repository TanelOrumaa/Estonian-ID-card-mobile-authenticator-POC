<template>
  <div class="container container-md d-flex flex-column">
    <div>
      <h3 class="text-center">Welcome to Estonian ID card mobile authentication demo website. When using an Android mobile phone, you can
        log in to the
        website using your ID card by using the button below.</h3>

      <p class="text-center">Read more from <a href="https://github.com/TanelOrumaa/Estonian-ID-card-mobile-authenticator-POC">here.</a></p>
    </div>
    <div id="canvas"></div>
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
    <div class="btn-group-sm d-flex justify-content-center" role="group" aria-label="Basic radio toggle button group">
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
  props: {
    "csrftoken": String,
    "csrfHeaderName": String,
  },
  data() {
    return {
      useAndroidApp: true,
      loading: false,
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
        this.$store.commit("setLoggedIn", true);
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
    }
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
