<template>
  <div ref="parent" v-if="$store.state.showPlayer">
    <div class="x-footer">
      <div>
        <div style="display: flex; box-sizing: border-box">
          <div class="duration">{{ format(currentPosition) }}</div>
          <div class="progresswrap" style="flex-grow: 1">
            <div class="progress">
              <div class="progress_bg">
                <div
                  class="video_progress"
                  :style="{ width: left + 'px' }"
                ></div>
              </div>
              <div class="progress_btn" :style="{ left: left + 'px' }"></div>
            </div>
          </div>
          <div class="duration">{{ format(duration) }}</div>
        </div>
        <div style="display: flex">
          <div class="x-meida">
            <div class="x-meida-img">
              <img
                :src="$store.state.playController.coverUrl"
                style="border-radius: 50%; width: 80px; height: 80px"
                :class="{ running: playing }"
              />
            </div>
            <div class="x-media-name">
              <h3>
                {{ $store.state.playController.name }}
                <span v-if="$store.state.playController.len > 1"
                  >-{{ 1 + $store.state.playController.curIndex }}/{{
                    $store.state.playController.len
                  }}</span
                >
              </h3>
              <h5></h5>
            </div>
          </div>
          <div class="x-media-btn">
            <font-awesome-icon
              :icon="['far', playing ? 'stop-circle' : 'play-circle']"
              class="bt"
              size="lg"
              @click="togglePlay"
            />
          </div>
          <div class="x-media-menu"></div>
        </div>

        <div class="ctrl" style="display: block; text-align: left; margin: 5px">
          <span class="bt" id="va" @click="sendEvent('input keyevent 24')"
            >音量+</span
          >
          <span class="bt" id="vm" @click="sendEvent('input keyevent 25')"
            >音量-</span
          >
          <font-awesome-icon
            :icon="modelIcon()"
            class="bt"
            style="float: right"
            fixed-width
            @click="changeMode()"
          />
          <span class="bt" id="next" style="float: right" @click="next"
            >下一个</span
          >
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions } from "vuex";
export default {
  props: ["curAid"],
  data() {
    return {
      left: 0,
    };
  },
  mounted: function () {
    this.initProgressBar();
    setTimeout(() => {
      $("#app").css("margin-bottom", $(".x-footer").outerHeight() + 10);
    }, 3000);

    (async () => {
      if (location.href.indexOf("localhost") == -1)
        for (;;) {
          try {
            await this.getStatus();
          } catch (e) {
            console.log(e);
          }

          await new Promise((resolve, reject) => {
            setTimeout(resolve, 2000);
          });
        }
    })();

    let sf = this;
    document.documentElement.addEventListener("touchstart", function (event) {
      //多根手指同时按下屏幕，禁止默认行为
      if (event.touches.length > 1) {
        event.preventDefault();
      }
    });
    document.documentElement.addEventListener(
      "touchend",
      function (event) {
        console.log(event);
        if (event.target.id) {
          switch (event.target.id) {
            case "va":
              sf.sendEvent("input keyevent 24");

              break;
            case "vm":
              sf.sendEvent("input keyevent 25");

              break;
            case "next":
              sf.next();

              break;
          }

          event.preventDefault();
        }
      },
      false
    );
    //阻止双指放大页面
    document.documentElement.addEventListener("gesturestart", function (event) {
      event.preventDefault();
    });
  },
  computed: {
    curItem() {
      return this.$store.state.playController.curItem;
    },
    playing() {
      return this.$store.state.playController.playing;
    },
    mode() {
      return this.$store.state.playController.mode;
    },
    duration() {
      return this.$store.state.playController.duration;
    },
    currentPosition() {
      return this.$store.state.playController.currentPosition;
    },
  },
  methods: {
    ...mapActions(["updateStatus"]),
    changeMode() {
      this.cmd({ cmd: "mode", val: (this.mode + 1) % 3 });
    },
    modelIcon() {
      if (this.mode == 0) return "recycle";
      else if (this.mode == 1) return "random";
      else if (this.mode == 2) return "subscript";
    },
    getStatus() {
      return fetch("/api/status", {
        method: "GET",
      })
        .then((response) => {
          response.json().then((data) => {
            this.updateStatus(data);
          });
        })
        .catch(function (e) {
          console.log("error: " + e.toString());
        });
    },
    next() {
      this.$store.dispatch("cmdAction", { cmd: "next" });
    },

    prev() {},
    onProgress(progress) {
      this.$store.dispatch("cmdAction", { cmd: "seekTo", val: progress });
    },
    togglePlay() {
      this.$store.dispatch("cmdAction", { cmd: "toggle" });
    },
    cmd(params) {
      this.$store.dispatch("cmdAction", params);
    },
    sendEvent(str) {
      fetch("/api/event", {
        method: "post",
        body: JSON.stringify({ event: str }),
        headers: {
          "Content-type": "application/x-www-form-urlencoded",
        },
      })
        .then((data) => {
          return data.text();
        })
        .then((val) => {
          console.log(val); //  输出请求到的字符串
        });
    },
    format(timeMs) {
      let totalSeconds = timeMs / 1000;

      let seconds = Math.trunc(totalSeconds % 60);
      let minutes = Math.trunc((totalSeconds / 60) % 60);
      let hours = Math.trunc(totalSeconds / 3600);

      if (hours > 0) {
        return `${hours}:${minutes}:${seconds}`;
      } else {
        return `${minutes}:${seconds}`;
      }
    },
    initProgressBar() {
      var my = this;
      var left = 0;
      $(function () {
        var tag = false,
          ox = 0,
          bgleft = 0;
        var progresswidth = $(".progress").width();
        $(".progress_btn").mousedown(function (e) {
          ox = e.pageX - my.left;
          console.log(ox);
          tag = true;
        });
        $(".progress").mouseup(function () {
          tag = false;
          my.onProgress(parseInt((my.left / progresswidth) * my.duration));
        });

        $(".progress").mousemove((e) => {
          //鼠标移动
          if (tag) {
            left = e.pageX - ox;
            if (left <= 0) {
              left = 0;
            } else if (left > progresswidth) {
              left = progresswidth;
            }
            console.log(left);
            my.left = left;
            // $(".progress_btn").css("left", left);

            //my.curProgress = (this.duration * left) / progresswidth;
            $(".text").html(parseInt((left / progresswidth) * 100) + "%");
          }
        });
        $(".progress").click(function (e) {
          //鼠标点击
          if (!tag) {
            bgleft = $(".progress").offset().left;
            left = e.pageX - bgleft;
            if (left <= 0) {
              left = 0;
            } else if (left > progresswidth) {
              left = progresswidth;
            }
            my.left = left;
            my.onProgress(parseInt((my.left / progresswidth) * my.duration));

            $(".text").html(parseInt((left / progresswidth) * 100) + "%");
            //my.curProgress = (this.duration * left) / progresswidth;
          }
        });
      });
    },
  },
  watch: {
    currentPosition() {
      this.left =
        ($(".progress").width() * this.currentPosition) / this.duration;
    },
  },
};
</script>

<style scoped>
.x-footer {
  position: fixed;
  background-color: gray;
  width: 100%;
  justify-content: space-between;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 5px;
  z-index: 10;
}

.x-meida {
  display: inline-flex;
  max-height: 100px;
  overflow: hidden;
}

.x-meida-img {
  width: 88px;
  height: 88px;
  overflow: hidden;
  border-radius: 5px;
}
img {
  width: 100%;
}
.x-media-name {
  padding: 10px 20px;
  color: #ffffff;
  text-align: left;
  flex: 1;
}
.x-media-btn {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: 3px solid #ffffff;
  padding: 10px;
  align-self: center;
}
.x-media-menu {
  display: inline-flex;
  width: 43px;
  height: 43px;
  align-self: center;
}

.x-mp3-progress {
  width: 100%;
  position: absolute;
  height: 6px;
  overflow: hidden;
  bottom: 0;
  left: 0;
}
.x-now-progress {
  position: relative;
  height: 6px;
  background-color: orange;
}

.progress {
  position: relative;
  width: 100%;
}
.progress .progress_bg {
  height: 13px;
  overflow: hidden;
  background-color: rgba(255, 255, 255, 0.3);
  transform: scale(1);
  transition: 0.2s;
}
.progress .progress_bg .video_progress {
  background-color: #31c27c;
  width: 50%;
  height: 100%;
}
.progress .progress_btn {
  width: 13px;
  height: 13px;
  position: absolute;
  background-color: red;
  left: 0px;
  top: 0;
  margin-left: 0;
  cursor: pointer;
  border-radius: 50%;
  /*transform: scale(0);*/
}
.duration {
  display: inline-flex;
}
.progresswrap {
  display: flex;
  flex-grow: 2;
}
.ctrl > * {
  margin: 5px;
  display: inline-block;
}
.bt {
  cursor: pointer;
}
@-webkit-keyframes img {
  from {
    -webkit-transform: rotate(0deg);
  }
  to {
    -webkit-transform: rotate(360deg);
  }
}
img {
  animation: img 8s linear infinite;
  -moz-animation: img 8s linear infinite;
  -webkit-animation: img 8s linear infinite;
  -o-animation: img 8s linear infinite;
  animation-play-state: paused;
}
.running {
  animation-play-state: running;
}
.progress {
  cursor: pointer;
}
</style>
