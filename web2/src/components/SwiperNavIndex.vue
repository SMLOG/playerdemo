<template>
  <div style="">
    <header class="header">
      <div class="menus" style="max-height: 50vh; overflow: auto">
        <ul v-show="showMenu" class="list-group">
          <li class="list-group-item">
            <div class="d-flex justify-content-between">
              <span>Show edit button</span
              ><span><input type="checkbox" v-model="showEdit" /></span>
            </div>
          </li>
          <li class="list-group-item">
            <div class="d-flex justify-content-between">
              <span>startAtBoot</span
              ><span
                ><input
                  type="checkbox"
                  v-model="startAtBoot"
                  @change="toggleStartAtBoot()"
              /></span>
            </div>
          </li>
          <li v-for="task in tasks" :key="task.id" class="list-group-item">
            <div class="d-flex justify-content-between">
              <div class="d-flex w-100 justify-content-between flex-grow">
                <div style="text-align: left">
                  {{ task.name }}- {{ task.duration / 1000 / 3600 }}h-
                  {{ task.lastRunAt | fmtDate }}
                </div>
                <div class="flex-shrink-0 me-3">
                  <span @click="changeTask(task, 'toggle')" class="me-3">{{
                    task.enable ? "Enabled" : "Disabled"
                  }}</span>
                  <a
                    v-if="task.enable"
                    @click="changeTask(task, 'run')"
                    class="col text-end"
                  >
                    <span v-if="task.status == 0">Run</span>
                    <span v-else-if="task.status == 1">Run ...</span>
                    <span v-else-if="task.status == 2">wait...</span>
                  </a>
                </div>
              </div>
              <span class="flex-column">
                <input
                  type="checkbox"
                  v-model="task.show"
                  @change="changeTask(task, 'show')"
                />
              </span>
            </div>
          </li>

          <li class="list-group-item">
            <div class="row">
              <span class="col text-start d-flex align-items-md-center"
                ><label class="form-label">Search:</label>
                <div style="width: 100%">
                  <input
                    class="form-control"
                    v-model="searchValue2"
                    @blur="search()"
                  /></div
              ></span>
            </div>
          </li>
        </ul>
      </div>

      <div>
        <h4 style="margin: 5px">
          宝贝计划
          <font-awesome-icon
            :icon="showMenu ? 'angle-up' : 'angle-down'"
            fixed-width
            @click="showMenu = !showMenu"
          />
        </h4>
      </div>
    </header>

    <div style="margin-top: 80px">
      <div class="swiper-box">
        <div class="swiper-container">
          <div class="swiper-wrapper">
            <div
              class="swiper-slide"
              v-for="(item, index) in tabs"
              :key="index"
            >
              <keep-alive>
                <component :ref="'c' + index" :is="item.comp"></component>
              </keep-alive>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ByVideo2 from "./ByVideo2";

import Swiper from "swiper";
import "swiper/swiper-bundle.css";
var mySwiper;
export default {
  components: {
    ByVideo2,
  },
  data() {
    return {
      searchValue2: "",
      showTest: false,
      curTabIndex: 0,
      showMenu: 0,
      tasks: [{ name: "test", status: 1, enable: 1, show: 1 }],
      startAtBoot: 0,
      tabs: [{ name: "视频学习", comp: "ByVideo2", label: "video" }],
    };
  },
  props: ["typeId"],

  mounted() {
    for (let i = 0; i < this.tabs.length; i++) {
      if (this.tabs[i].label == this.typeId) this.curTabIndex = i;
    }
    mySwiper = new Swiper(".swiper-container", {
      initialSlide: 0,
    });
    mySwiper.on("slideChange", () => {
      // 监控滑动后当前页面的索引，将索引发射到导航组件
      // 左右滑动时将当前slide的索引发送到nav组件
      this.curTabIndex = mySwiper.activeIndex;
      //this.$root.eventHub.$emit("slideTab", mySwiper.activeIndex);
    });
    // 接收nav组件传过来的导航按钮索引值，跳转到相应内容区
    /*this.$root.eventHub.$on("changeTab", (index) => {
      // 点击导航键跳转相应内容区
      mySwiper.slideTo(index, 0, false);
    });
    console.log(mySwiper);*/

    this.$nextTick(() => {
      document.addEventListener("scroll", this.handleScroll, true);
    });
    this.getTasks();
    this.getBootStatus();
  },
  filters: {
    fmtDate(lo) {
      if (lo <= 0) return "no run yet";
      let d = new Date(lo);
      return (
        d.getMonth() +
        1 +
        "-" +
        d.getDate() +
        " " +
        d.getHours() +
        ":" +
        d.getMinutes()
      );
    },
  },
  computed: {
    detach() {
      return this.$store.state.playController.detach;
    },
    showEdit: {
      get() {
        return this.$store.state.showEdit;
      },
      set(value) {
        this.$store.commit("showEdit", value);
      },
    },
    searchValue: {
      get() {
        return this.$store.state.searchValue;
      },
      set(value) {
        console.log(value);
        this.$store.commit("searchValue", value);
      },
    },
  },
  methods: {
    getBootStatus() {
      $.getJSON("/api/boot").then((r) => {
        this.startAtBoot = r.startAtBoot;
      });
    },
    toggleStartAtBoot() {
      $.post("/api/boot").then((r) => {
        this.getBootStatus();
        // this.startAtBoot = r.startAtBoot;
      });
    },
    changeTask(task, action) {
      $.post("/api/tasks/update", { id: task.id, action: action }).then(() => {
        this.getTasks();
      });
      setTimeout(() => {
        this.getTasks();
      }, 3000);
    },

    getTasks() {
      $.getJSON("/api/tasks").then((res) => {
        this.tasks.length = 0;
        Object.keys(res).forEach((r) => {
          this.tasks.push(res[r]);
        });
      });
    },
    search() {
      this.searchValue = this.searchValue2;
    },
    cmd(params) {
      this.$store.dispatch("cmdAction", params);
    },
    handleScroll() {
      var scrollTop = 0;
      if (document.documentElement && document.documentElement.scrollTop) {
        scrollTop = document.documentElement.scrollTop;
      } else if (document.body) {
        scrollTop = document.body.scrollTop;
      }

      var clientHeight = 0;
      if (document.body.clientHeight && document.documentElement.clientHeight) {
        clientHeight = Math.min(
          document.body.clientHeight,
          document.documentElement.clientHeight
        );
      } else {
        clientHeight = Math.max(
          document.body.clientHeight,
          document.documentElement.clientHeight
        );
      }

      var scrollHeight = Math.max(
        document.body.scrollHeight,
        document.documentElement.scrollHeight
      );

      if (scrollTop + clientHeight == scrollHeight) {
        console.log("bottom");
      }
    },
  },
  watch: {
    curTabIndex() {
      this.$store.commit("showPlayer", true);
      $("html, body").animate({ scrollTop: 0 }, 0);
      mySwiper.slideTo(this.curTabIndex, 0, false);

      /*this.$router.push({
        path: "/home/" + this.tabs[this.curTabIndex].label + "/1",
      });*/
      this.$router.push({
        name: "home",
        params: {
          typeId: this.tabs[this.curTabIndex].label,
          pageId: this.$refs["c" + this.curTabIndex][0].page || 1,
        },
      });
    },
  },
};
</script>

<style scoped>
.tab {
  cursor: pointer;
}
.curTab {
  background: white;
}

.curTab:after {
  content: "";
  width: auto;
  min-width: 44px;
  height: 2px;
  background: #4e6ef2;
  border-radius: 1px;
  display: block;
  margin-top: 1px;
}
ul {
  list-style-type: none;
  padding: 0;
}
.tabs li {
  display: inline-block;
  margin: 0 10px;
}
.menus ul {
  padding: 0;
  margin: 0;
}
.menus li {
  background: gray;
  color: white;
}
.menus a {
  background: gray;
  color: white;
}
.header {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  background: rgba(255, 255, 255, 0.8);
  z-index: 1000;
}
</style>
