# User Management

You can preinstall up to three users via the Ant build target _generate_configs_. For that purpose the users must be defined in the Ant property file _etc/ant_configuration/user.properties_ and the corresponding password in _etc/startup/passphrases.txt_. For preinstalling more users you will have to adapt the _build.xml_ in the root directory accordingly.

It is also possible to manually add LAS2peer user agents to an existing network. Please refer to the LAS2peer Template Project and LAS2peer for more information.

Currently the OCD Service implements no method that allows to add, change or erase users via the service interface.
