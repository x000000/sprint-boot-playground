<template>
  <div class="toast">
    <div v-if="!header" class="toast-body d-flex" :class="flavorClass">
      {{ text }}
      <button v-if="closeButton" type="button"
              class="btn-close me-2 m-auto"
              :class="typeof closeButton === 'string' ? closeButton : []"
              data-bs-dismiss="toast"></button>
    </div>
    <template v-else>
      <div class="toast-header d-flex" :class="flavorClass">
        <strong class="me-auto">{{ header }}</strong>
        <button v-if="closeButton" type="button"
                class="btn-close me-2 m-auto"
                :class="typeof closeButton === 'string' ? closeButton : []"
                data-bs-dismiss="toast"></button>
      </div>
      <div class="toast-body">{{ text }}</div>
    </template>
  </div>
</template>

<script>
export default {
  name: "Toast",
  props: {
    text: String,
    header: String,
    flavorClass: [ String, Array ],
    closeButton: [ Boolean, String ],
  }
}
</script>

<style scoped>

</style>