#Code by Josef Wallace for Networking A1
import signal
import sys
from socket import *
from optparse import OptionParser
from datetime import *
from threading import *

#Globals
cache_active = False
block_list_active = False
# Cache & Blocklist
# Random values for initialization
cache = {'key' : ('Time', b'Something')}
block_list = {'block_list_item'}
cache.clear()
block_list.clear()

# Signal handler for pressing ctrl-c
def ctrl_c_pressed(signal, frame):
	sys.exit(0)

#Read bytes from client
#Return: If request was valid, method, path, version, headers
def read_request_bytes(client_connection, recv_bytes):
    #Error responses
    response_400_error = 'HTTP/1.0 400 Bad Request\r\n\r\n'
    response_501_error = 'HTTP/1.0 501 Not Implemented\r\n\r\n'
    response_403_error = 'HTTP/1.0 403 Forbidden\r\n\r\n'
    
    #Storage of important data
    method = ''
    protocol = ''
    ip = ''
    path_to_object = ''
    port = ''
    version = ''
    headers = {}
    
    request_lines = recv_bytes.decode().split('\r\n')
    http_request = request_lines[0]
    
    #Checks for three arguments
    if (len(http_request.split()) != 3):
        client_connection.sendall(response_400_error.encode())
        return False, method, ip, path_to_object, port, version, headers
    
    #Initial parsing
    http_request_parameters = http_request.split()
    method = http_request_parameters[0]
    protocol_split = http_request_parameters[1].split('://')
    protocol = protocol_split[0]
    
    #Ensure correct protocol
    if (protocol != 'http'):
        client_connection.sendall(response_400_error.encode())
        return False, method, ip, path_to_object, port, version, headers
    
    version = http_request_parameters[2]
    
    #Ensure correct Method
    if (method != 'GET'):
        client_connection.sendall(response_501_error.encode())
        return False, method, ip, path_to_object, port, version, headers
        
    #Ensure correct HTTP version
    if (version != 'HTTP/1.0'):
        client_connection.sendall(response_400_error.encode())
        return False, method, ip, path_to_object, port, version, headers
        
    address = protocol_split[1].split(':')
    
    #Various checks to ensure the URL is valid
    if (len(address) == 2):
        path_parts = address[1].split("/")
        
        if (len(path_parts) <= 1):
            client_connection.sendall(response_400_error.encode())
            return False, method, ip, path_to_object, port, version, headers
        
        for i in range(1, len(path_parts)):
            path_to_object += '/' + path_parts[i]
            
        ip = address[0]
        port = int(path_parts[0])
    else:
        path_parts = address[0].split('/')
        
        if (len(path_parts) <= 1):
            client_connection.sendall(response_400_error.encode())
            return False, method, ip, path_to_object, port, version, headers
        
        for i in range(1, len(path_parts)):
            path_to_object +=  '/' + path_parts[i]
            
        ip = path_parts[0]
        port = 80
    
    for line in request_lines[1:]:
        if line:
            if (len(line.split(': ')) != 2 or ' ' in line.split(': ')[0]):
                client_connection.sendall(response_400_error.encode())
                return False, method, ip, path_to_object, port, version, headers
            key, value = line.split(': ')
            headers[key] = value
            
    #Enforce server connection close
    headers['Connection'] = 'close'
    
    #No need to complete other operations if this is in fact an operation request
    if check_for_operation_path(path_to_object):
        return False, method, ip, path_to_object, port, version, headers
    
    #Block Checking
    url = http_request.split()[1]
    if block_list_active:
        for item in block_list:
            if item in url:
                client_connection.sendall(response_403_error.encode())
                return False, method, ip, path_to_object, port, version, headers
            
    return True, method, ip, path_to_object, port, version, headers

#perfoms absolute path operations given a path
def check_for_operation_path(path):
    #To modify globals
    global cache_active
    global block_list_active
    global cache
    global block_list
    
    if (path == "/proxy/cache/enable"):
        cache_active = True
        return True
    elif (path == "/proxy/cache/disable"):
        cache_active = False
        return True
    elif (path == "/proxy/cache/flush"):
        cache.clear()
        return True
    elif (path == "/proxy/blocklist/enable"):
        block_list_active = True
        return True
    elif (path == "/proxy/blocklist/disable"):
        block_list_active = False
        return True
    elif (path.startswith("/proxy/blocklist/add/")):
        path = path.removeprefix("/proxy/blocklist/add/")
        block_list.add(path)
        return True
    elif (path.startswith("/proxy/blocklist/remove/")):
        path = path.removeprefix("/proxy/blocklist/remove/")
        block_list.remove(path)
        return True
    elif (path == "/proxy/blocklist/flush"):
        block_list.clear()
        return True
    return False

#Ensures everything is received
def receive_all(receiving_socket):
    data = b''
    while True:
        piece = receiving_socket.recv(1024)
        data += piece
        if piece.endswith(b'\r\n\r\n') or piece == b'':
            break
    return data

#Method used for each new client thread
def handle_client(client_socket):
    client_request = receive_all(client_socket)
    success, method, server_ip, path_to_object, server_port, version, headers = read_request_bytes(client_socket, client_request)

    #Don't continue if invalid request
    if (not success):
        client_socket.close()
        return

    #Assemble Proxy request to send to origin server
    proxy_request = method + ' ' + path_to_object + ' ' + 'HTTP/1.0\r\n'
    proxy_request += 'Host: ' + server_ip + '\r\n'
    for key in headers:
        proxy_request += key + ': ' + headers[key] + '\r\n'
        
    #duplicate before modification
    cache_key = proxy_request
    
    #Receive all data from server
    server_response = b''
    with socket(AF_INET, SOCK_STREAM) as origin_server_skt:
        if proxy_request in cache.keys() and cache_active:
            #grab last accessed time
            last_access_time = cache[cache_key][0]
            proxy_request += 'If-Modified-Since: ' + last_access_time + '\r\n\r\n'
            origin_server_skt.connect((server_ip, server_port))
            origin_server_skt.sendall(proxy_request.encode())
            server_response = receive_all(origin_server_skt)
            
            if '304 Not Modified' in server_response.decode():
                client_socket.sendall(cache[cache_key][1])
            else:
                #format time and save in cache
                current_time = datetime.utcnow()
                formatted_time = current_time.strftime('%a, %d %b %Y %H:%M:%S GMT')
                cache[cache_key] = (formatted_time, server_response)
                client_socket.sendall(server_response)
            #Done with this socket
            origin_server_skt.close()
        else:
            #Finish request
            proxy_request += '\r\n'
            
            #Send and Receive
            origin_server_skt.connect((server_ip, server_port))
            origin_server_skt.sendall(proxy_request.encode())
            server_response = receive_all(origin_server_skt)
            
            #Format time and save data in cache
            current_time = datetime.utcnow()
            formatted_time = current_time.strftime('%a, %d %b %Y %H:%M:%S GMT')
            cache[cache_key] = (formatted_time, server_response)
            client_socket.sendall(server_response)
            
            #close socket
            origin_server_skt.close()

    #Done with this socket
    client_socket.close()

# Start of program execution
# Parse out the command line server address and port number to listen to
parser = OptionParser()
parser.add_option('-p', type='int', dest='serverPort')
parser.add_option('-a', type='string', dest='serverAddress')
(options, args) = parser.parse_args()

port = options.serverPort
address = options.serverAddress
if address is None:
    address = 'localhost'
if port is None:
    port = 2100

# Set up signal handling (ctrl-c)
signal.signal(signal.SIGINT, ctrl_c_pressed)

#Student section of main
with socket(AF_INET, SOCK_STREAM) as skt:
    skt.setsockopt(SOL_SOCKET, SO_REUSEADDR, 1)
    skt.bind((address, port))
    skt.listen()
    
    while True: 
            client_connection, client_address = skt.accept()
            thread = Thread(target=handle_client, args=[client_connection])
            thread.start()
        