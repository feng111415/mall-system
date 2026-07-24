<script>
export default {
  onLaunch() {}
}
</script>

<style>
page {
  min-height: 100%;
  color: #3a3a3a;
  background: #fcf9f9;
  font-family: "PingFang SC", "Source Han Sans SC", "Microsoft YaHei UI", "Microsoft YaHei", sans-serif;
  font-weight: 400;
  letter-spacing: 2rpx;
}

.page-shell {
  position: relative;
  min-height: 100vh;
  padding: 40rpx;
  box-sizing: border-box;
  overflow: hidden;
  background:
    radial-gradient(circle at 72rpx 80rpx, rgba(216, 184, 194, 0.14), transparent 180rpx),
    linear-gradient(180deg, #fcf9f9 0%, #fdf9f4 100%);
}

.page-shell::before {
  content: "";
  position: absolute;
  left: 0;
  top: 24rpx;
  width: 100%;
  height: 220rpx;
  opacity: 0.06;
  pointer-events: none;
  background:
    repeating-linear-gradient(180deg, transparent 0 20rpx, #a98d96 21rpx 22rpx, transparent 23rpx 38rpx),
    linear-gradient(90deg, transparent 0 72%, #3a3a3a 72% 74%, transparent 74% 77%, #3a3a3a 77% 79%, transparent 79%);
}

.page-shell > view,
.page-shell > button,
.page-shell > image {
  position: relative;
  z-index: 1;
}

.soft-card {
  border-radius: 16rpx;
  background: rgba(255, 250, 246, 0.92);
  border: 1rpx solid rgba(234, 226, 232, 0.58);
  box-shadow: 0 14rpx 38rpx rgba(216, 184, 194, 0.12);
}

.primary-button {
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 20rpx;
  color: #fffafa;
  background: linear-gradient(135deg, #e0c6d0 0%, #d8b8c2 100%);
  box-shadow: 0 16rpx 32rpx rgba(216, 184, 194, 0.22);
  font-size: 28rpx;
  font-weight: 300;
}

.ghost-button {
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 12rpx;
  color: #3a3a3a;
  background: rgba(255, 253, 253, 0.78);
  border: 1rpx solid rgba(216, 184, 194, 0.36);
  font-size: 26rpx;
  font-weight: 300;
}

.muted-text {
  color: rgba(153, 153, 153, 0.82);
}

.piano-page-title {
  color: #d8b8c2;
  font-size: 34rpx;
  font-weight: 200;
  line-height: 1.35;
}

.piano-section-title {
  color: #3a3a3a;
  font-size: 28rpx;
  font-weight: 300;
  line-height: 1.5;
}

.piano-body-text {
  color: #3a3a3a;
  font-size: 24rpx;
  font-weight: 400;
  line-height: 1.7;
}

.piano-help-text {
  color: rgba(153, 153, 153, 0.82);
  font-size: 22rpx;
  font-weight: 300;
  line-height: 1.7;
}

.input-line {
  height: 82rpx;
  padding: 0;
  border-bottom: 1rpx solid #eae2e8;
  color: #3a3a3a;
  background: transparent;
  font-size: 26rpx;
  font-weight: 400;
  box-sizing: border-box;
}

.textarea-soft {
  width: 100%;
  padding: 22rpx;
  border-radius: 12rpx;
  color: #3a3a3a;
  background: rgba(234, 226, 232, 0.28);
  font-size: 24rpx;
  line-height: 1.7;
  box-sizing: border-box;
}

.placeholder {
  color: rgba(153, 153, 153, 0.72);
  font-weight: 300;
}
</style>
