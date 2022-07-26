export const mymixin = {
  data() {
    return {
      page: 1,
      total: 0,
    };
  },
  created() {},
  methods: {},
  computed: {},
  mounted: function() {
    this.page = parseInt(this.$route.params.pageId);
  },
  watch: {
    page() {
      this.$router.push({
        // name: this.$route.name,
        params: {
          //typeId: this.$route.params.typeId,
          pageId: this.page,
        },
      });
    },
  },
};
