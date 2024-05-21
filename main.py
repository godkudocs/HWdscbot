# ![main.py] made by godkudocs 2024

# !--- Built in modules ---!
import subprocess
import datetime
import os

# !--- External modules/files ---!
# coming soon.


# Define the Java source file and output log file
java_file = "bot.java"
log_file = "run.log"
jar_file = "bot.jar"

# Function to compile the Java file
def compile_java(java_file):
    compile_command = ["javac", java_file]
    try:
        result = subprocess.run(compile_command, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        return True, result.stdout.decode(), result.stderr.decode()
    except subprocess.CalledProcessError as e:
        return False, e.stdout.decode(), e.stderr.decode()

# Function to create a JAR file from the compiled classes
def create_jar(jar_file, java_class):
    jar_command = ["jar", "cf", jar_file, f"{java_class}.class"]
    try:
        result = subprocess.run(jar_command, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        return True, result.stdout.decode(), result.stderr.decode()
    except subprocess.CalledProcessError as e:
        return False, e.stdout.decode(), e.stderr.decode()

# Function to run the JAR file
def run_jar(jar_file, log_file):
    run_command = ["java", "-jar", jar_file]
    with open(log_file, "w") as log:
        try:
            result = subprocess.run(run_command, check=True, stdout=log, stderr=log)
            return True
        except subprocess.CalledProcessError as e:
            log.write(e.stdout.decode())
            log.write(e.stderr.decode())
            return False

# Get the current date and time
current_time = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")

# Log the start time
with open(log_file, "a") as log:
    log.write(f"Run started at: {current_time}\n")

# Compile the Java file
compiled, compile_stdout, compile_stderr = compile_java(java_file)

with open(log_file, "a") as log:
    if compiled:
        log.write("Compilation successful.\n")
        log.write(compile_stdout)
    else:
        log.write("Compilation failed.\n")
        log.write(compile_stderr)
        log.write("Run aborted due to compilation errors.\n")
        exit(1)

# Extract the class name (assuming it's the same as the file name without .java)
java_class = java_file.split(".")[0]

# Create the JAR file
jar_created, jar_stdout, jar_stderr = create_jar(jar_file, java_class)

with open(log_file, "a") as log:
    if jar_created:
        log.write("JAR file creation successful.\n")
        log.write(jar_stdout)
    else:
        log.write("JAR file creation failed.\n")
        log.write(jar_stderr)
        log.write("Run aborted due to JAR creation errors.\n")
        exit(1)

# Run the JAR file
ran_successfully = run_jar(jar_file, log_file)

# Log the completion status
with open(log_file, "a") as log:
    if ran_successfully:
        log.write("Run completed successfully.\n")
    else:
        log.write("Run encountered errors.\n")

# Log the end time
end_time = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
with open(log_file, "a") as log:
    log.write(f"Run ended at: {end_time}\n")
