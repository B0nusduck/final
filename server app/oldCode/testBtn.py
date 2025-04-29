from gpiozero import Button
from time import sleep

#button = Button(16)
button = Button(12)

while True:
	if not button.is_pressed:
		print("click")
		sleep(0.5)
