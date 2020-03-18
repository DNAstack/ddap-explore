version 1.0

workflow hello {
	input {
		String name
	}

	call sayHello {
		input:
			name = name
	}

	output {
		String greeting = sayHello.greeting
	}
}

task sayHello {
	input {
		String name
	}

	command {
		echo "Hello ~{name}"
	}

	output {
		String greeting = read_string(stdout())
	}

	runtime {
		docker: "ubuntu:xenial"
	}
}
