<template>
  <Navbar/>
  <LoginComponent/>
</template>

<script>
import LoginComponent from '@/components/Login'
import Navbar from "@/components/Navbar";

export default {
  name: 'Login',
  components: {
    LoginComponent,
    Navbar
  },
  methods: {
    csrf_token: function () {
      return "csrf-token";
    }
  }
}
</script>
