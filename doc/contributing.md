# Contributing

The source of truth remote repository is stored on [BitBucket](https://bitbucket.org/martinwheatman/enguage). There is a mirror of the repository on [GitHub](https://github.com/martinwheatman/enguage).

You can clone the code with `git clone git@bitbucket.org:martinwheatman/enguage.git`

## Set up mirror remote

`git remote add origin git@bitbucket.org:martinwheatman/enguage.git`
`git remote set-url origin git@github.com:martinwheatman/enguage.git`

This should use BitBucket as the source of truth and also push to GitHub on pushes to BitBucket.

If you merge a PR on BitBucket, you should do a pull/push locally, to sync GitHub.

# The Repo

The main source code of Enguage exists in `/org/enguage`.

The Enguage repertoires exist in `/etc/rpts`.

Some existing consumers of the the main library exist in `/opt`.
