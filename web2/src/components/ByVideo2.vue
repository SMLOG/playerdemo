<template>
  <div class="hello">
    <div style="text-align: left; margin-left: 10px">
      <a href="https://smlog.github.io/h/" target="_blank"> 健康</a>
      <b> 频道:</b>
      <a
        class="channel"
        v-for="ch in channels"
        @click="selectChannel(ch.id)"
        :key="ch.id"
        :class="{ cur: ch.id == curChannelId }"
        >{{ ch.name }}</a
      >
      <span style="margin-left: 20px" v-if="searchValue"
        >过滤条件:{{ searchValue }}
        <b style="cursor: pointer" @click="searchValue = ''">x</b></span
      >
    </div>
    <div style="clear: both">
      <ul>
        <li
          class="it"
          :class="{ showAll_k: showAll_k == k }"
          v-for="(it, k) in folders"
          :key="it.id"
          style="position: relative"
        >
          <img
            class="coverUrl"
            :src="it.coverUrl"
            referrerPolicy="no-referrer"
            style="cursor: pointer"
            @click="toggleFav(it)"
          />
          <font-awesome-icon
            v-if="it.isFav"
            :icon="['fas', 'star']"
            fixed-width
            class="star"
          />
          <div style="margin-left: 5px">
            <div v-if="!it.deleted">
              <b>#{{ k }}</b
              ><span @click="showAll_k = showAll_k == k ? -1 : k"
                >{{ it.title || it.name }}:({{ it.files.length }})</span
              >
              <span
                v-if="$store.state.showEdit"
                style="cursor: pointer"
                @click="delFolder(it)"
                >x</span
              >
            </div>
            <div>Rate:{{ it.rate }} updateTime:{{ it.updateTime }}</div>
            <div>
              <li v-for="(file, i) in it.files" :key="file.p">
                <span
                  @click="play(file)"
                  class="url"
                  :title="file.name"
                  :class="{ cache: file.name }"
                >
                  <div>
                    <span class="num">{{ i + 1 }}({{ file.playCnt }})</span
                    ><span>{{ file.name }}</span>
                  </div>
                </span>
                <em v-if="isPreview" class="url" @click="openUrl(k, i)">*</em>
                <em
                  v-if="$store.state.showEdit"
                  class="url"
                  @click="delFile(it, file, i + 1)"
                  >x</em
                >
              </li>
            </div>
          </div>
        </li>
      </ul>
    </div>
    <pagination
      style="padding-left: 10px"
      v-model="page"
      :per-page="20"
      :options="{ chunk: 5 }"
      :records="total"
      @paginate="loadList"
    />
  </div>
</template>

<script>
import { mapActions } from "vuex";
import { mymixin } from "./common.mixin";
//import debounce from "./util";
export default {
  mixins: [mymixin],
  data: () => {
    return {
      pageSize: 0,
      isPlaying: false,
      curPosition: 0,
      percent: 0,
      duration: 0,
      folders: [],
      progress: 0,
      updateRemote: 0,
      showAll_k: -1,
      isEdit: false,
      isPreview: false,
      channels: [],
      curChannelId: -1,
    };
  },

  computed: {
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
  activated: function () {
    this.loadChannels().then(() => {
      if (this.curChannelId <= 0 && this.channels.length > 0) {
        this.curChannelId = this.channels[0].id;
      }
    });
  },
  props: {
    msg: String,
  },
  methods: {
    toggleFav(it) {
      $.getJSON("/api/toggleFav?id=" + it.id).then((res) => {
        it.isFav = res.isFav;
      });
    },
    selectChannel(id) {
      this.curChannelId = id;
    },
    ...mapActions(["updateStatus"]),
    updateProgress(percent, isPlay) {
      this.curPosition = percent * this.duration;
      this.isPlaying = isPlay;
      this.updateRemote++;
    },
    openUrl() {},
    delFile(folder, file, i) {
      confirm("delete foler " + folder.name + " - " + (file.name || i)) &&
        $.post("/api/delFile", { id: file.id }).then((data) => {
          if (data == "ok") {
            file.deleted = 1;
          } else alert(data);
        });
    },
    delFolder(folder) {
      confirm("delete foler " + folder.name) &&
        $.post("/api/delFolder", { id: folder.id }).then((data) => {
          if (data == "ok") {
            folder.deleted = 1;
          } else alert(data);
        });
    },
    base64(item) {
      let n = "data:image/png;base64," + item.thumb;
      console.log(n);
      return n;
    },
    play(file) {
      this.$store.dispatch("cmdAction", {
        cmd: "play",
        id: file.id,
        typeId: 3,
      });
    },
    loadChannels() {
      return $.getJSON("/api/channels").then((data) => {
        for (let k in data) {
          this.channels.push({ name: k, id: data[k] });
        }
        console.log(this.channels);
      });
    },
    loadList() {
      $.getJSON("/api/manRes", {
        page: this.page,
        typeId: this.curChannelId,
        searchValue: this.$store.state.searchValue,
      }).then((data) => {
        this.folders.length = 0;
        this.total = data.total;
        this.pageSize = data.pageSize;
        this.folders.push(...data.datas);
        $("html, body").animate({ scrollTop: 0 }, 0);
      });
    },
  },
  watch: {
    curChannelId() {
      this.page = 1;
      this.loadList();
    },
    "$store.state.searchValue": {
      deep: true,
      handler: function () {
        clearTimeout(this.debounceTimeout);
        this.debounceTimeout = setTimeout(() => {
          this.page = 1;
          this.loadList();
        }, 1000);
      },
    },
  },
};
</script>

<style scoped>
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #42b983;
}
li.it {
  display: flex;
  text-align: left;
  max-height: 100px;
  overflow: hidden;

  padding-top: 10px;
}
.coverUrl {
  width: 100px;
  height: 100px;
  min-width: 100px;
  min-height: 100px;
}
.url {
  margin: 5px;
  float: left;
  cursor: pointer;
}
.cur {
  background: gray;
}
.bt {
  cursor: pointer;
  margin: 5px 3px;
}
.showAll_k.it {
  max-height: 100%;
}
li.active {
  background: #eee;
}
.cache {
  text-decoration: underline;
}
.channel {
  display: inline-block;
  margin: 5px;
  padding: 3px;
}
.num {
  display: inline-block;
  color: gray;
  margin-right: 10px;
  padding: 3px;
  text-decoration: gray;
}
.star {
  position: absolute;
  top: 10px;
  left: 0;
  color: red;
}
</style>
