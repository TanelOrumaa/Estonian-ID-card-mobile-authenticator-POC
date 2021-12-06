<template>
  <Navbar/>
  <hello-world v-bind:csrftoken="csrf_token()"/>
</template>

<script>
import HelloWorld from '../../components/Login.vue'
import Navbar from "@/components/Navbar";

export default {
  name: 'App',
  components: {
    HelloWorld,
    Navbar
  },
  methods: {
    csrf_token: function () {
      return "csrf-token";
    }
  }
}
</script>
