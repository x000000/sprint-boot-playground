<template>
<div class="panel">
  <nav class="navbar bg-light shadow">
    <div class="container">
      <a class="navbar-brand" href="#">
        <i class="bi-files"></i> CSV Explorer
      </a>

      <div class="d-flex">
        <form ref="form" :action="uploadEndpoint" method="post" enctype="multipart/form-data" @submit.prevent="upload">
          <fieldset :disabled="loading">
            <button v-if="records?.length > 0" type="button" class="btn btn-outline-danger" @click="deleteAll">Delete all</button>
            <label v-else class="btn btn-outline-primary">
              <input type="file" name="file" @change="onFilePick" />
              Upload
            </label>
          </fieldset>
        </form>

      </div>
    </div>
  </nav>

  <div class="position-relative">
    <div class="toast-container position-absolute top-0 end-0 p-3" ref="toasts"></div>
  </div>

  <div class="container">
    <table class="table table-striped table-hover">
      <thead>
        <tr>
          <th scope="col">#</th>
          <th scope="col">Source</th>
          <th scope="col">Code List Code</th>
          <th scope="col">Code</th>
          <th scope="col">Display Value</th>
          <th scope="col">Long Description</th>
          <th scope="col">From Date</th>
          <th scope="col">To Date</th>
          <th scope="col">Sorting Priority</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="entry in records">
          <th scope="row">{{ entry.id }}</th>
          <td>{{ entry.source }}</td>
          <td>{{ entry.codeListCode }}</td>
          <td>{{ entry.code }}</td>
          <td>{{ entry.displayValue }}</td>
          <td>{{ entry.longDescription }}</td>
          <td>{{ entry.fromDate }}</td>
          <td>{{ entry.toDate }}</td>
          <td>{{ entry.sortingPriority }}</td>
        </tr>
      </tbody>
    </table>
  </div>

</div>
</template>

<script>
import {createApp} from 'vue';
import {default as Axios} from 'axios';
import Toast from './Toast.vue';

const axios = Axios;
const baseUrl = '/api/csv';

export default {
  data() {
    return {
      records: null,
      loading: false,
      uploadEndpoint: baseUrl,
    };
  },
  created() {
    this.loadRecords();
  },
  methods: {
    handleError(e) {
      const resp = e.response?.data;
      const msg  = resp?.message || resp || e.message || e;
      console.error(msg);

      const toastApp = createApp(Toast, {
        text: msg,
        header: 'Error!',
        flavorClass: [ 'text-white bg-danger' ],
        closeButton: 'btn-close-white',
      });
      const toastVm = toastApp.mount(this.$refs.toasts.appendChild(document.createElement('div')));
      toastVm.$el.addEventListener('hidden.bs.toast', () => {
        toastApp.unmount();
        toastApp._container.remove();
      });

      new bootstrap.Toast(toastVm.$el).show();
    },
    loadRecords() {
      this.loading = true;
      axios.get(baseUrl)
        .then(({ data }) => this.records = data)
        .catch(this.handleError)
        .finally(() => this.loading = false);
    },
    onFilePick() {
      this.$refs.form.requestSubmit();
    },
    async upload() {
      const form = this.$refs.form;
      await fetch(form.action, {
        method: form.method,
        body: new FormData(form),
      });
      this.loadRecords();
    },
    async deleteAll() {
      await axios.delete(this.$refs.form.action);
      this.loadRecords();
    }
  }
}
</script>

<style lang="scss" scoped>
input[type=file] {
  display: none;
}
table .buttons {
  display: inline-grid;
}
.spinner-border {
  margin-right: .2em;
}
.toast-container {
  z-index: 10000;
}
</style>