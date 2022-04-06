<template>
<div class="panel">
  <nav class="navbar bg-light shadow">
    <div class="container">
      <a class="navbar-brand" href="#">
        <i class="bi-wallet2"></i> Kuehne + Nagel Wallet
      </a>

      <div class="d-flex">
        <button :disabled="loading"
                type="button"
                class="btn btn-outline-primary"
                data-bs-toggle="modal"
                data-bs-target="#create-form">Create</button>
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
          <th scope="col">Name</th>
          <th scope="col">Surname</th>
          <th scope="col">Balance</th>
          <th scope="col"></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="wl in wallets">
          <th scope="row">{{ wl.id }}</th>
          <td>{{ wl.name }}</td>
          <td>{{ wl.surname }}</td>
          <td>€{{ wl.balance }}</td>
          <td>
            <fieldset class="d-inline-flex gap-2" :disabled="loading">
              <button type="button" class="btn btn-outline-primary btn-sm"
                      @click="wallet = wl"
                      data-bs-toggle="modal"
                      data-bs-target="#topup-form">Top Up</button>
              <button type="button" class="btn btn-outline-primary btn-sm"
                      @click="wallet = wl"
                      data-bs-toggle="modal"
                      data-bs-target="#withdraw-form">Withdraw</button>
              <button type="button" class="btn btn-outline-primary btn-sm"
                      @click="wallet = wl"
                      data-bs-toggle="modal"
                      data-bs-target="#transfer-form">Transfer</button>
            </fieldset>
          </td>
        </tr>
      </tbody>
    </table>
  </div>

  <div class="modal fade" id="create-form" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1">
    <div class="modal-dialog">
      <form class="modal-content" @submit.prevent="tryCreate" :disabled="loading">
        <div class="modal-header">
          <h5 class="modal-title">Create wallet</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <div class="mb-3">
            <label for="name-input" class="form-label">Name</label>
            <input type="text" class="form-control" id="name-input" v-model="name">
          </div>
          <div class="mb-3">
            <label for="surname-input" class="form-label">Surname</label>
            <input type="text" class="form-control" id="surname-input" v-model="surname">
          </div>
        </div>
        <div class="modal-footer">
          <button type="submit" class="btn btn-primary">
            <i v-if="loading" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></i>
            Create
          </button>
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" ref="closeBtn">Close</button>
        </div>
      </form>
    </div>
  </div>

  <div class="modal fade" id="topup-form" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1">
    <div class="modal-dialog">
      <form class="modal-content" @submit.prevent="tryTopUp" :disabled="loading">
        <div class="modal-header">
          <h5 class="modal-title">Top up wallet #{{ wallet.id }}</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <div class="mb-3">
            <label for="topup-input" class="form-label">Amount</label>
            <input type="number" min="0" step="0.01" class="form-control" id="topup-input" v-model="wallet.amount">
          </div>
        </div>
        <div class="modal-footer">
          <button type="submit" class="btn btn-primary">
            <i v-if="loading" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></i>
            Confirm
          </button>
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
        </div>
      </form>
    </div>
  </div>

  <div class="modal fade" id="withdraw-form" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1">
    <div class="modal-dialog">
      <form class="modal-content" @submit.prevent="tryWithdraw" :disabled="loading">
        <div class="modal-header">
          <h5 class="modal-title">Withdraw money from wallet #{{ wallet.id }}</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <div class="mb-3">
            <label for="withdraw-input" class="form-label">Amount</label>
            <input type="number" min="0" step="0.01" class="form-control" id="withdraw-input" v-model="wallet.amount">
          </div>
        </div>
        <div class="modal-footer">
          <button type="submit" class="btn btn-primary">
            <i v-if="loading" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></i>
            Confirm
          </button>
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
        </div>
      </form>
    </div>
  </div>

  <div class="modal fade" id="transfer-form" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1">
    <div class="modal-dialog">
      <form class="modal-content" @submit.prevent="tryTransfer" :disabled="loading">
        <div class="modal-header">
          <h5 class="modal-title">Transfer money from wallet #{{ wallet.id }}</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <div class="mb-3">
            <label for="transfer-input" class="form-label">Amount</label>
            <input type="number" min="0" step="0.01" class="form-control" id="transfer-input" v-model="wallet.amount">
          </div>
        </div>
        <div class="modal-body">
          <div class="mb-3">
            <label for="transfer-wallet" class="form-label">Target wallet ID</label>
            <input type="number" min="1" class="form-control" id="transfer-wallet" v-model="targetWallet">
          </div>
        </div>
        <div class="modal-footer">
          <button type="submit" class="btn btn-primary">
            <i v-if="loading" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></i>
            Confirm
          </button>
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
        </div>
      </form>
    </div>
  </div>
</div>
</template>

<script>
import {createApp} from 'vue';
import {default as Axios} from 'axios';
import Toast from './Toast.vue';

const axios = Axios;
const baseUrl = '/api/wallets';

export default {
  data() {
    return {
      wallets: null,
      wallet: {},
      targetWallet: null,
      name: '',
      surname: '',
      loading: false,
    };
  },
  created() {
    this.loadWallets();
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
    loadWallets() {
      this.loading = true;
      axios.get(baseUrl)
        .then(({ data }) => this.wallets = data)
        .catch(this.handleError)
        .finally(() => this.loading = false);
    },
    tryCreate() {
      this.loading = true;
      axios.post(baseUrl, undefined, {
        params: {
          name: this.name,
          surname: this.surname,
        },
      })
      .then(_ => {
        this.loadWallets();
        this.$refs.closeBtn.click();
      })
      .catch(e => {
        this.loading = false;
        this.handleError(e);
      });
    },
    tryTopUp() {
      this.tryModifyBalance(this.wallet, +this.wallet.amount, 'topup');
    },
    tryWithdraw() {
      this.tryModifyBalance(this.wallet, +this.wallet.amount, 'withdraw');
    },
    tryTransfer() {
      const target = +this.targetWallet;
      this.tryModifyBalance(this.wallet, +this.wallet.amount, 'transfer/' + target)
        .then(_ => {
          axios.get(baseUrl + '/' + target)
            .then(({ data }) => {
              this.loading = true;
              const wallet = this.wallets.find(w => w.id === target);
              wallet && (wallet.balance = data);
              this.loading = false;
            })
            .catch(_ => this.loadWallets())
        });
    },
    tryModifyBalance(wallet, amount, sfx) {
      if (!amount) {
        return this.handleError('Amount is required');
      }

      this.loading = true;
      return axios.put(baseUrl + '/' + wallet.id + '/' + sfx, undefined, {
        params: { amount },
      })
      .then(({ data }) => {
        wallet.balance = data;
        this.$root.$el.querySelector('.modal.show button[data-bs-dismiss=modal]').click();
      })
      .catch(this.handleError)
      .finally(() => this.loading = false);
    }
  }
}
</script>

<style lang="scss" scoped>
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