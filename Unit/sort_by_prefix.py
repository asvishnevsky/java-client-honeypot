import os

def main():
	path = 'Z:\\sites'
	for d in os.listdir(path):
		if len(d) <= 2:
			continue
		print('Processing {}'.format(d))
		prefix = d[:2]
		target = os.path.join(path, prefix)
		if not os.path.exists(target):
			os.mkdir(target)
		print('Moving {} to {}'.format(os.path.join(path, d), os.path.join(target, d))) 
		os.rename(os.path.join(path, d), os.path.join(target, d))

if __name__ == '__main__':
	main()