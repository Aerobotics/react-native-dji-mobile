module.exports = {
  env: {
    browser: true,
    es6: true,
    'react-native/react-native': true,
  },

  extends: [
    'eslint:recommended',
    'plugin:react/recommended',
    'plugin:react-native/all',
    'plugin:flowtype/recommended',
  ],

  parser: 'babel-eslint',

  parserOptions: {
    ecmaFeatures: {
      jsx: true,
    },
    ecmaVersion: 2018,
    sourceType: 'module',
  },

  plugins: [
    'react',
    'react-native',
    'flowtype',
  ],

  rules: {
    'flowtype/require-valid-file-annotation': ['error', 'always'],
    'flowtype/newline-after-flow-annotation': ['error', 'always'],
    'flowtype/no-types-missing-file-annotation': ['off'],
    'flowtype/no-weak-types': ['error'],

    'semi': ['error', 'always'],
    'no-multi-spaces': ['error'],
    'no-trailing-spaces': ['error'],
    'indent': ['error', 2, { 'SwitchCase': 1 }],
    'quotes': ['error', 'single'],
    'comma-dangle': ['error', 'always-multiline'],
    'comma-spacing': ['error'],
    'object-curly-newline': ['error', { 'minProperties': 1 }],
    'object-property-newline': ['error'],
    'no-unused-vars': ['warn'],

    'no-underscore-dangle': ['error', { 'allowAfterThis': true, 'allowAfterSuper': true }],

    'react-native/no-inline-styles': ['off'],
    'react-native/no-color-literals': ['off'],
  },
};
