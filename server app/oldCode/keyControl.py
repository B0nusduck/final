import sys
import uartControl as uart
import readkey as rk
from time import sleep
import time
import threading
import l298Control as crt

class Timer:  # Using a class to manage state better

    def __init__(self):
        self.start_time = None
        self.running = False
        self.thread = None

    def start(self):
        if self.running:
            return  # Timer already running

        self.start_time = time.time()
        self.running = True
        self.thread = threading.Thread(target=self._print_elapsed_time)
        self.thread.daemon = True  # Allow the main thread to exit even if the timer thread is running
        self.thread.start()

    def _print_elapsed_time(self):
        while self.running:
            elapsed_time = time.time() - self.start_time
            print(f"Elapsed time: {elapsed_time:.2f} seconds", end="\r")  # \r overwrites the previous line
            time.sleep(0.1)  # Adjust printing frequency as needed

    def end(self):
        if not self.running:
            return

        self.running = False
        self.thread.join()  # Wait for the printing thread to finish
        elapsed_time = time.time() - self.start_time
        print(f"\nTotal elapsed time: {elapsed_time:.2f} seconds")  # Final message
        self.start_time = None  # Reset for next use.

timer = Timer()
speed = 250000
spin_speed = 250000
port = uart.connect("/dev/ttyUSB0")
while True:
	key = rk.read()
	match key:
		case 'q':
			uart.move2(port, 0, 0)
			crt.allOff
			sys.exit()
			timer.end()
		case 'w':
			uart.move2(port, speed, speed)
		case 'a':
			uart.move2(port, -spin_speed, spin_speed)
		case 's':
			uart.move2(port, -speed, -speed)
		case  'd':
			uart.move2(port, spin_speed, -spin_speed)
		case '5':
			crt.moveOne(False, 100)
			timer.start()
		case '2':
			crt.moveOne(True, 70)
			timer.start()
		case '1':
			uart.move4(port, speed, -speed, -speed, speed)
		case '3':
			uart.move4(port, -speed, speed, speed, - speed)
		case _:
			uart.move2(port, 0, 0)
			crt.oneOff()
			timer.end()
