Examplary client program arguments:
--Ice.Config=config/config.adam
--Ice.Config=config/config.jan
--Ice.Config=config/config.ula
--Ice.Config=config/config.ala

Server program arguments:
--Ice.Config=config/config.server

Required:
-ZeroC Ice https://zeroc.com/distributions/ice 
-Ice Builder https://github.com/zeroc-ice/ice-builder-eclipse "Configuring the pluggin" and below
-Window -> Preferences -> IceBuilder (specify path, e.g.: C:\Program Files (x86)\ZeroC\Ice-<version>)
-import Ice.jar (lib directory under Ice-<version>)
	right click on the project -> Build Path -> Configure Build Path -> Libraries -> Add external jar