export default {
  plugins: {
    autoprefixer: {},
    'postcss-px-to-viewport-8-plugin': {
      viewportWidth: 375,
      viewportHeight: 667,
      unitPrecision: 5,
      viewportUnit: 'vw',
      selectorBlackList: ['ignore'],
      minPixelValue: 1,
      mediaQuery: false,
    },
  },
}