
import sys
from time import sleep
import uartControl as uart
import readkey as rk

port = uart.connect("/dev/ttyUSB0")

while True:
	try:
		char = rk.read()
	except:
		char = 'x'
	match char:
		case 'q':
			uart.move2(port, 0, 0)
			sys.exit()
		case 'w':
			uart.move2(port, 500000, 500000)
		case 'a':
			uart.move2(port, -500000, 500000)
		case 's':
			uart.move2(port, -500000, -500000)
		case 'd':
			uart.move2(port, 500000, -500000)
		case '4':
			uart.move4(port, 500000, -500000, -500000, 500000)
		case '6':
			uart.move4(port, -500000, 500000, 500000, -500000)
		case _:
			uart.move2(port, 0, 0)
