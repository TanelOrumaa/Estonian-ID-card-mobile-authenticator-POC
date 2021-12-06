<template>
  <!-- As a heading -->
  <nav class="navbar navbar-dark bg-dark container-fluid flex-row">
    <div class="">
      <span class="navbar-brand mb-0 h1">Mobile authentication demo</span>
    </div>
    <div v-if="isLoggedIn" class="nav-item">
      <button type="button" class="btn btn-light" v-on:click="logOut">Log out</button>
    </div>
  </nav>
</template>

<script>
import router from "@/router";

export default {
  name: "Navbar",
  computed: {
    isLoggedIn() {
      return this.$store.getters.getAuthenticated;
    }
  },
  methods: {
    logOut: function () {
      this.$store.commit("setLoggedIn", false);
      router.push("/");
    }
  },
  mounted() {
    if (this.$store.getters.getSessionId == null) {
      const sessionId = this.$cookie.getCookie("JSESSIONID");
      this.$store.dispatch("fetchSessionId", sessionId);
    }
  }
}
</script>

<style scoped>
  nav {
    height: 5vh;
  }
</style>