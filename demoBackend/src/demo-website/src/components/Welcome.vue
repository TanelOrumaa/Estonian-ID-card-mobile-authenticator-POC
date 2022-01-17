<template>
  <div class="container container-md d-flex flex-column">
    <div>
      <h3 class="text-center">Welcome {{ userName }}!</h3>
      <h4 class="text-center">{{ userIdCode }}</h4>
      <p class="text-center">You've successfully logged into this site using your ID card.</p>
      <p class="text-center">Read more from <a
          href="https://github.com/TanelOrumaa/Estonian-ID-card-mobile-authenticator-POC">here.</a></p>
    </div>
  </div>

</template>


<script>
export default {
  name: 'WelcomeComponent',
  props: {},
  methods: {
    getUserData: async function () {
      const requestOptions = {
        method: "GET",
        headers: {
          "sessionid": this.$store.getters.getSessionId
        }
      };
      fetch("/auth/userData", requestOptions)
          .then((response) => {
                let data = response.body;
                data.getReader().read().then((body) => {
                  let authObject = JSON.parse(new TextDecoder().decode(body.value));
                  this.$store.dispatch("setUserName", authObject.userData.name);
                  let idCode = authObject.userData.idCode.substring(6)
                  console.log(idCode)
                  this.$store.dispatch("setUserIdCode", idCode);
                });
                console.log(data);
              }
          );
    },
  },
  computed: {
    isLoggedIn() {
      return this.$store.getters.getAuthenticated;
    },
    userName() {
      return this.$store.getters.getUserName;
    },
    userIdCode() {
      return this.$store.getters.getUserIdCode;
    }
  }
  ,
  mounted() {
    // Get user data.
    this.getUserData();
  }
}
</script>

<style scoped>
div {
  margin-top: 2vh;
}
</style>
