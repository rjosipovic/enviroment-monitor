import requests
from sense_hat import SenseHat
import subprocess

sense = SenseHat()

humidity = sense.get_humidity()
temp = sense.get_temperature()
pressure = sense.get_pressure()

cpu_temp_str = subprocess.check_output("vcgencmd measure_temp", shell=True)
array = cpu_temp_str.split("=")
array2 = array[1].split("'")
cpu_temp = float(array2[0])

temp_calibrated = temp - ((cpu_temp - temp)/5.466)

req_str = 'http://dorat:8080/EnviromentCollector/webresources/collector/mesure?temperature=%s&presure=%s&humidity=%s' % (temp_calibrated, pressure, humidity)