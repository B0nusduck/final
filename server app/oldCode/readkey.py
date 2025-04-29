import tty
import sys
import termios

def read():
	orig_settings = termios.tcgetattr(sys.stdin)
	tty.setcbreak(sys.stdin)
	x = sys.stdin.read(1)[0]
	print(x)
	termios.tcsetattr(sys.stdin, termios.TCSADRAIN, orig_settings)
	return x
