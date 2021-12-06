<template>
  <Navbar/>
  <WelcomeComponent/>
</template>

<script>
import WelcomeComponent from '@/components/Welcome'
import Navbar from "@/components/Navbar";

export default {
  name: 'Welcome',
  components: {
    WelcomeComponent,
    Navbar
  },
  methods: {
    csrf_token: function () {
      return "csrf-token";
    }
  }
}
</script>
