from bluedot.btcomm import BluetoothServer
from signal import pause

def data_received(data):
	print(data)
	s.send(data)

s = BluetoothServer(data_received, True, "hci0", 1, "utf-8", True, None, None)
pause()
