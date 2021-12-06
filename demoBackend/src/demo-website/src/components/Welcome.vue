<template>
  <div class="container container-md d-flex flex-column">
    <div>
      <h3 class="text-center">Congratulations, you logged into the site. Log out to try again.</h3>
      <p class="text-center">Read more from <a href="https://github.com/TanelOrumaa/Estonian-ID-card-mobile-authenticator-POC">here.</a></p>
    </div>
  </div>

</template>


<script>
export default {
  name: 'WelcomeComponent',
  props: {
    "csrftoken": String,
  },
  computed: {
    isLoggedIn() {
      return this.$store.getters.getAuthenticated;
    }
  }
}
</script>

<style scoped>
div {
  margin-top: 2vh;
}
</style>
