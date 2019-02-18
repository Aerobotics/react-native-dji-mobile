#!/usr/bin/env node

const fs = require('fs');
const chokidar = require('chokidar');

const DO_NOT_COPY_PATHS = [
  /node_modules/,
  /^(\..+)/,
  /(.*)(~\.m)/,
  /(.*)(~\.swift)/,
  /ios\/Pods/,
  /example/,
  /scripts/,
  /.DS_STORE/,
];

const DEV_PATH = process.env.DEV_PATH;

if (typeof DEV_PATH === 'undefined') {
  console.log('Please specify absolute DEV_PATH');
  return;
}

// One-liner for current directory, ignores .dotfiles
chokidar
  .watch('.', { ignored: DO_NOT_COPY_PATHS })
  .on('all', (event, fullpath) => {

    if (event === 'unlink') {
      fs.unlink(fullpath); // TODO: What about deleting directories?

    } else if (event === 'add' || event === 'change') {
      const filepath = fullpath.substring(0, fullpath.lastIndexOf('/'));
      const filename = fullpath.substring(fullpath.lastIndexOf('/') + 1);
      console.log(event, fullpath);
      // console.log(filepath, filename);
  
      fs.mkdirSync(`${DEV_PATH}/${filepath}`, { recursive: true }, err => {
        // console.log(err);
      });
  
      fs.copyFileSync(fullpath, `${DEV_PATH}/${filepath}/${filename}`);
    }
});
