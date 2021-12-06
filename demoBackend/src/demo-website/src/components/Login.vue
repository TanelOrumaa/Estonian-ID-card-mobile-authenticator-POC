<template>
  <div class="container container-md d-flex flex-column">
    <div>
      <h3 class="text-center">Welcome to Estonian ID card mobile authentication demo website. When using an Android mobile phone, you can
        log in to the
        website using your ID card by using the button below.</h3>

      <p class="text-center">Read more from <a href="https://github.com/TanelOrumaa/Estonian-ID-card-mobile-authenticator-POC">here.</a></p>
    </div>
    <div class="justify-content-center d-flex">
      <button type="button" class="btn btn-lg btn-dark" v-on:click="authenticate">Authenticate</button>
    </div>
    <div class="btn-group-sm d-flex justify-content-center" role="group" aria-label="Basic radio toggle button group">
      <input type="radio" class="btn-check" name="btnradio" id="btnCardReader" autocomplete="off" v-on:click="useCardReader">
      <label class="btn btn-outline-secondary" for="btnCardReader">using ID-card reader</label>

      <input type="radio" class="btn-check" name="btnradio" id="btnApp" autocomplete="off" checked v-on:click="useApp">
      <label class="btn btn-outline-secondary" for="btnApp">using Android App</label>
    </div>
    <div id="canvas"></div>
    <p>Token: {{ csrftoken }}</p>
  </div>

</template>


<script>
import * as webeid from '../web-eid.js';

export default {
  name: 'Login',
  props: {
    "csrftoken": String,
  },
  data() {
    return {
      useApp: true,

    }
  },
  methods: {
    useApp: function() {
      this.useApp = true;
    },

    useCardReader: function() {
      this.useApp = false;
    },

    authenticate: async function () {
      const csrfToken = document.querySelector('#csrftoken').content;
      const csrfHeaderName = document.querySelector('#csrfheadername').content;

      const options = {
        getAuthChallengeUrl: window.location.origin + "/auth/challenge",
        postAuthTokenUrl: window.location.origin + "/auth/login",
        getAuthSuccessUrl: window.location.origin + "/auth/login",
        useAuthApp: this.useApp,
        headers: {
          [csrfHeaderName]: csrfToken
        }
      };

      console.log(options);

      try {
        const response = await webeid.authenticate(options);
        console.log("Authentication successful! Response:", response);

        window.location.href = "/welcome";

      } catch (error) {
        console.log("Authentication failed! Error:", error);
        throw error;
      }
    }
  },
  mounted() {
    fetch("/auth/challenge")
        .then((response) => response.text()
        ).then((data) => {
          console.log(data)
          this.msg = data
        }
    )
  }
}
</script>

<style scoped>
div {
  margin-top: 2vh;
}
</style>
