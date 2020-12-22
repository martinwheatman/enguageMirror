# flatpak - A Container System

A containerised (flatpak) app remains work in progress.<br/>
This requires:
<ul>
<li> apt install flatpak
<li> apt install flatpak-builder
<li> flatpak install flathub org.freedesktop.Platform//19.08 org.freedesktop.Sdk//19.08
<li> flatpak install flathub org.freedesktop.Sdk.Extension.openjdk11
<li> flatpak-builder --user --install --force-clean inst org.enguage.Eng.yaml
<li> flatpak run --versbose org.enguage.Eng hello
</ul>

## eng.yaml

Please see enguage/app/flatpak/org.enguage.Eng.yaml
