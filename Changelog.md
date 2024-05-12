DebugUtils 1.0.5
================
- Simplify command. Remove `/debugutils server` as that's the default now

DebugUtils 1.0.4
================
- Add player bound toggles:
    - `/debugutils server` will toggle the debug features on the server (and if done via a player also for the player)
    - `/debugutils player` will toggle the feature for the passed in players. If this feature is not enabled 
       via `server` first this will do nothing.
  
DebugUtils 1.0.3
================
- Fix refmap entry missing

DebugUtils 1.0.2
================
- Add api to register more debug renderers 
- Add debug renderer to show the spawn chunks

DebugUtils 1.0.1
================
- Fix mixin refmap missing