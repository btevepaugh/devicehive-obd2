**DeviceHive OBD2 Gateway for OBD2 compatible cars**
--------------------------------

DeviceHive OBD2 Gateway collects vehicle's self-diagnostic information and sends it to the cloud. This gateway works with Android devices and Bluetooth ELM327 adaptors. Video demo - https://www.youtube.com/watch?v=JnoyO4KPGJA

![](obd2-architecture.png?raw=true)

**Usage**
------------------
- Pair your ELM327 adaptor with you device using the system Bluetooth settings.
- Install and run this app.
- Enter DeviceHive server credentials and choose your ELM327 from the list of paired devices.
- Click on `Start service` and the app will log OBD2 data to cloud.

**Commands**
------------------
There are few commands which can be executed from DeviceHive server:
- `GetTroubleCodes` - read OBD2 trouble codes and return them in the command results. No parameters required.
- `RunCommand` - run arbitrary OBD2 command and get the results. `mode` and `pid` parameters should be specified in command parameters in a single byte hex string with leading zero. Example `{"mode":"01","pid":"0C"}` ('Engine RPM' PID).

**Freeboard example**
------------------
`Freeboard-RPM-Throttle-example.json` is a config for basic examples with Freeboard ( https://github.com/devicehive/freeboard ). Freeboard is also aviliable at DeviceHive playgrounds. Usage: open file in Freeboard, click on datasource and insert your AccessKey, observe your engine RPM and Throttle on the graphs.

**Local storage**
------------------
Not implemented yet

**Legacy**
------------------
This project is based on:
- DeviceHive Android BLE gateway - https://github.com/devicehive/android-ble
- OBD-II Java API - https://github.com/pires/obd-java-api

**DeviceHive license**
------------------

DeviceHive is developed by DataArt Apps and distributed under Open Source MIT license. This basically means you can do whatever you want with the software as long as the copyright notice is included. This also means you don't have to contribute to the end product or revert the changes back to Open Source, but if you feel like sharing, you are highly encouraged to do so!

