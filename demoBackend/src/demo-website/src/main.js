import {createApp} from 'vue';
import App from './App.vue';
import {createStore} from 'vuex'
import BootstrapVue3 from 'bootstrap-vue-3'
import createPersistedState from "vuex-persistedstate";

import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue-3/dist/bootstrap-vue-3.css'
import router from "./router/index";

// Create a new store instance.
const store = createStore({
    state() {
        return {
            authenticated: false,
        }
    },
    mutations: {
        setLoggedIn(state, isLoggedIn) {
            console.log("Setting logged in: " + isLoggedIn);
            state.authenticated = isLoggedIn;
        }
    },
    getters: {
      getAuthenticated: state => {
          return state.authenticated;
      }
    },
    plugins: [createPersistedState()],
})

router.beforeEach((to, from, next) => {
    if (to.matched.some(record => record.meta.requiresAuth)) {
        // this route requires auth, check if logged in
        // if not, redirect to login page.
        if (!store.state.authenticated) {
            next({name: 'Login'})
        } else {
            next() // go to wherever I'm going
        }
    } else {
        next() // does not require auth, make sure to always call next()!
    }
})

const app = createApp(App)
app.use(BootstrapVue3)
app.use(router)
app.use(store)
app.mount('#app')