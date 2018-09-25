from sense_hat import SenseHat
import subprocess

sense = SenseHat()

humidity = sense.get_humidity()
temp = sense.get_temperature()

cpu_temp_str = subprocess.check_output("vcgencmd measure_temp", shell=True)
array = cpu_temp_str.split("=")
array2 = array[1].split("'")
cpu_temp = float(array2[0])

temp_calibrated = temp - ((cpu_temp - temp)/5.466)

pressure = sense.get_pressure()

print("temperature:%s" % temp)
print("CPU temp:%s" % cpu_temp)
print("temp_calibrated:%s" % temp_calibrated)
print("humidity:%s" % humidity)
print("pressure:%s" % pressure)
