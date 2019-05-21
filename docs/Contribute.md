# Contribute to react-native-dji-mobile

## Fork Github repository
```shell
$ git clone git@github.com:Aerobotics/react-native-dji-mobile.git
$ cd react-native-dji-mobile
$ git fork 
```

## Update source code from Aerobotics repository
### Init upstream
```shell
$ cd react-native-dji-mobile
$ git remote add upstream git@github.com:Aerobotics/react-native-dji-mobile.git
$ git fetch upstream
```

### Merge Aerobotics develop branch to your working branch
then: (like "git pull" which is fetch + merge)
```shell
$ git merge upstream/develop your_branch
```

or, better, replay your local work on top of the fetched branch like a "git pull --rebase"
```shell
$ git rebase upstream/develop
```

### Pull request
TODO
