import {createApp} from 'vue';
import App from './App.vue';
import {createStore} from 'vuex';
import BootstrapVue3 from 'bootstrap-vue-3';
import createPersistedState from "vuex-persistedstate";
import { VueCookieNext } from 'vue-cookie-next'


import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue-3/dist/bootstrap-vue-3.css'
import router from "./router/index";

// Create a new store instance.
const store = createStore({
    state() {
        return {
            authenticated: false,
            jSessionId: null,
            isAndroid: false,
            userName: null,
            userIdCode: null,
        }
    },
    mutations: {
        setLoggedIn(state, isLoggedIn) {
            state.authenticated = isLoggedIn;
        },
        setSessionId(state, sessionId) {
            state.jSessionId = sessionId;
        },
        setIsAndroid(state, isAndroid) {
            state.isAndroid = isAndroid;
        },
        setUserName(state, userName) {
            state.userName = userName;
        },
        setIdCode(state, idCode) {
            state.userIdCode = idCode;
        }
    },
    actions: {
        fetchSessionId(context, sessionId) {
            context.commit("setSessionId", sessionId);
        },
        setLoggedIn(context, isLoggedIn) {
            context.commit("setLoggedIn", isLoggedIn);
        },
        setIsAndroid(context, isAndroid) {
            context.commit("setIsAndroid", isAndroid);
        },
        setUserName(context, userName) {
            context.commit("setUserName", userName);
        },
        setUserIdCode(context, userIdCode) {
            context.commit("setIdCode", userIdCode);
        }
    },
    getters: {
        getAuthenticated: state => {
            return state.authenticated;
        },
        getSessionId: state => {
            return state.jSessionId;
        },
        getIsAndroid: state => {
            return state.isAndroid;
        },
        getUserName: state => {
            return state.userName;
        },
        getUserIdCode: state => {
            return state.userIdCode;
        },
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
app.use(VueCookieNext);
app.mount('#app')

VueCookieNext.config({ expire: '7d' })
