<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import QRCode from 'qrcode'
</script>

<style lang="scss" scoped>
.main-box {
  display: flex;
  flex-direction: column;
}

.qr-code {
  cursor: pointer !important;
}

.qr-code:hover {
  opacity: .5;
}
</style>
