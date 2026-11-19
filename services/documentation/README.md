# Documentation

A web application based on Hugo and theme [hugo-theme-relearn](https://mcshelby.github.io/hugo-theme-relearn/)

[[_TOC_]]

---

## How to work on this project

### Software requirements

Check [deployed documentation](https://saas-g4it.com/documentation/1-getting-started/2-getting-started-as-a-developper/1-how-to-setup-my-local-dev-env/index.html)

### Import needed themes

cd services/documentation

git clone -b main --single-branch https://github.com/McShelby/hugo-theme-relearn.git themes/hugo-theme-relearn
git clone -b main --single-branch https://github.com/martignoni/hugo-video.git themes/hugo-video

### How to run locally

#### Development server

Use IntelliJ Run Configuration named `OpenSource Documentation`.

Or run `hugo server -D -p1233 --navigateToChanged` for a dev server.
This will open your browser on `http://localhost:1233/`.
The app will automatically reload if you change any of the source files.
