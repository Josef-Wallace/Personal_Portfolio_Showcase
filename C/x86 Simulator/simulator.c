/*
  Original Author: Danial Kopta
  Completed by: Josef Wallace for Computer Systems A1
*/

#include <stdio.h>
#include <stdlib.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>
#include "instruction.h"

// Forward declarations for helper functions
unsigned int get_file_size(int file_descriptor);
unsigned int* load_file(int file_descriptor, unsigned int size);
instruction_t* decode_instructions(unsigned int* bytes, unsigned int num_instructions);
unsigned int execute_instruction(unsigned int program_counter, instruction_t* instructions, 
				 unsigned int* registers, unsigned char* memory);
void print_instructions(instruction_t* instructions, unsigned int num_instructions);
void error_exit(const char* message);

// 17 registers
#define NUM_REGS 17
// 1024-byte stack
#define STACK_SIZE 1024

int main(int argc, char** argv)
{
  // Make sure we have enough arguments
  if(argc < 2)
    error_exit("must provide an argument specifying a binary file to execute");

  // Open the binary file
  int file_descriptor = open(argv[1], O_RDONLY);
  if (file_descriptor == -1) 
    error_exit("unable to open input file");

  // Get the size of the file
  unsigned int file_size = get_file_size(file_descriptor);
  // Make sure the file size is a multiple of 4 bytes
  // since machine code instructions are 4 bytes each
  if(file_size % 4 != 0)
    error_exit("invalid input file");

  // Load the file into memory
  // We use an unsigned int array to represent the raw bytes
  // We could use any 4-byte integer type
  unsigned int* instruction_bytes = load_file(file_descriptor, file_size);
  close(file_descriptor);

  unsigned int num_instructions = file_size / 4;

  //Helper
  unsigned char get_opcode(unsigned int instruction)
  {
    unsigned char opcode = instruction >> 27;
    return opcode;
  }

  //Helper
  unsigned char get_first_register(unsigned int instruction)
  {
    unsigned char first_register = instruction >> 22;
    first_register = first_register & 0x0000001f;
    return first_register;
  }

  //Helper
  unsigned char get_second_register(unsigned int instruction)
  {
    unsigned char second_register = instruction >> 17;
    second_register = second_register & 0x0000001f;
    return second_register;
  }

  //Helper
  short get_immediate(unsigned int instruction)
  {
    short immediate = instruction & 0x0000ffff;
    return immediate;
  }
  
  instruction_t* decode_instructions(unsigned int* instruction_bytes, unsigned int num_instructions)
  {
    instruction_t* output_array = (instruction_t*)malloc(sizeof(instruction_t) * num_instructions);
    
    for (int i = 0; i < num_instructions; i += 1)
    {
      instruction_t current_instruction;
      unsigned int bytes = *instruction_bytes;

      //Prepare for the next instruction by incrementing the pointer by 1
      instruction_bytes += 1;

      //Set values of instruction struct
      current_instruction.opcode = get_opcode(bytes);
      current_instruction.first_register = get_first_register(bytes);
      current_instruction.second_register = get_second_register(bytes);
      current_instruction.immediate = get_immediate(bytes);
      
      *output_array = current_instruction;
      output_array += 1;
    }
    
    //Decrement the pointer by the number of instructions
    output_array -= num_instructions;
    return output_array;
  }
  
  // Allocate and decode instructions (left for you to fill in)
  instruction_t* instructions = decode_instructions(instruction_bytes, num_instructions);
  
  // Allocate and initialize registers
  unsigned int* registers = (unsigned int*)malloc(sizeof(unsigned int) * NUM_REGS);
  // TODO: initialize register values

  for (int i = 0; i < NUM_REGS; i += 1)
  {
    registers[i] = 0;
  }
  registers[8] = 1024; //stack pointer

  // Stack memory is byte-addressed, so it must be a 1-byte type
  // TODO allocate the stack memory. Do not assign to NULL.
  unsigned char* memory = (unsigned char*)malloc(sizeof(unsigned char) * STACK_SIZE);

  for (int i = 0; i < STACK_SIZE; i += 1)
  {
    memory[i] = 0;
  }

  // Run the simulation
  unsigned int program_counter = 0;

  // program_counter is a byte address, so we must multiply num_instructions by 4 to get the address past the last instruction
  while(program_counter != num_instructions * 4)
  {
    program_counter = execute_instruction(program_counter, instructions, registers, memory);
  }

  return 0;
}
//Helpers
unsigned int get_CF(unsigned int register_content)
{
  if ((register_content & 0x00000001) == 0x00000001)
  {
    return 1;
  }
  return 0;
}

unsigned int get_ZF(unsigned int register_content)
{
  if ((register_content & 0x00000040) == 0x00000040)
  {
    return 1;
  }
  return 0;
}

unsigned int get_SF(unsigned int register_content)
{
  if ((register_content & 0x00000080) == 0x00000080)
  {
    return 1;
  }
  return 0;
}

unsigned int get_OF(unsigned int register_content)
{
  if ((register_content & 0x00000800) == 0x00000800)
  {
    return 1;
  }
  return 0;
}

/*
 * Executes a single instruction and returns the next program counter
*/
unsigned int execute_instruction(unsigned int program_counter, instruction_t* instructions, unsigned int* registers, unsigned char* memory)
{
  // program_counter is a byte address, but instructions are 4 bytes each
  // divide by 4 to get the index into the instructions array
  instruction_t instr = instructions[program_counter / 4];
  long result;
  unsigned long result_unsigned;
  int buffer = 0;
  
  switch(instr.opcode)
  {
  case subl:
    // cast the immediate up to a 32-bit signed type
    // signed overflow will not happen since one of the operands is unsigned
    registers[instr.first_register] = registers[instr.first_register] - (int)instr.immediate;
    break;
    
  case addl_reg_reg:
    registers[instr.second_register] = registers[instr.first_register] + registers[instr.second_register];
    break;

  case addl_imm_reg:
    registers[instr.first_register] = registers[instr.first_register] + (int)instr.immediate;
    break;

  case imull:
    registers[instr.second_register] = registers[instr.first_register] * registers[instr.second_register];
    break;

  case shrl:
    registers[instr.first_register] = registers[instr.first_register] >> 1;
    break;

  case movl_reg_reg:
    registers[instr.second_register] = registers[instr.first_register];
    break;

  case movl_deref_reg:
    buffer = ((unsigned int)memory[registers[instr.first_register] + (int)instr.immediate + 3]) << 24;
    buffer += ((unsigned int)memory[registers[instr.first_register] + (int)instr.immediate + 2]) << 16;
    buffer += ((unsigned int)memory[registers[instr.first_register] + (int)instr.immediate + 1]) << 8;
    buffer += (unsigned int)memory[registers[instr.first_register] + (int)instr.immediate];
    registers[instr.second_register] = buffer;
    break;

  case movl_reg_deref:
    memory[registers[instr.second_register] + (int)instr.immediate] = registers[instr.first_register];
    memory[registers[instr.second_register] + (int)instr.immediate + 1] = (registers[instr.first_register] >> 8);
    memory[registers[instr.second_register] + (int)instr.immediate + 2] = (registers[instr.first_register] >> 16);
    memory[registers[instr.second_register] + (int)instr.immediate + 3] = (registers[instr.first_register] >> 24);
    break;

  case movl_imm_reg:
    registers[instr.first_register] = (int)instr.immediate;
    break;

  case cmpl:
    registers[0] = 0;
    result = (long)((int)registers[instr.second_register]) - (long)((int)registers[instr.first_register]);
    result_unsigned = (unsigned long)registers[instr.second_register] - (unsigned long)registers[instr.first_register];
    //Check for unsigned overflow
    if (result_unsigned > (long)0x00000000ffffffff || registers[instr.second_register] < registers[instr.first_register])
    {
      registers[0] += 1;
    }
    //Check for 0 result
    if (result == 0)
    {
      registers[0] += 64;
    }
    //Check for sign bit
    if ((result & (long)0x0000000080000000) == (long)0x0000000080000000)
    {
      registers[0] += 128;
    }
    //Check for signed overflow
    if (result < (long)0xffffffff80000000 || result > (long)0x000000007fffffff)
    {
      registers[0] += 2048;
    }
    break;

  case je:
    if (get_ZF(registers[0]) == 1)
    {
      program_counter += (int)instr.immediate;
    }
    break;

  case jl:
    if (get_SF(registers[0]) ^ get_OF(registers[0]) == 1)
    {
      program_counter += (int)instr.immediate;
    }
    break;

  case jle:
    if (((get_SF(registers[0]) ^ get_OF(registers[0])) | get_ZF(registers[0])) == 1)
      {
	program_counter += (int)instr.immediate;
      }
    break;

  case jge:
    if ((get_SF(registers[0]) ^ get_OF(registers[0])) != 1)
      {
	program_counter += (int)instr.immediate;
      }
    break;

  case jbe:
    if (get_CF(registers[0]) | get_ZF(registers[0]) == 1)
      {
	program_counter += (int)instr.immediate;
      }
    break;

  case jmp:
    program_counter += (int)instr.immediate;
    break;
    
  case call:
    registers[8] -= 4;
    memory[registers[8]] = program_counter + 4;
    program_counter += (int)instr.immediate;
    break;

  case ret:
    if (registers[8] == 1024)
    {
      exit(0);
    }
    program_counter = memory[registers[8]];
    registers[8] += 4;
    return program_counter;

  case pushl:
    registers[8] -= 4;
    memory[registers[8]] = registers[instr.first_register];
    memory[registers[8] + 1] = registers[instr.first_register] >> 8;
    memory[registers[8] + 2] = registers[instr.first_register] >> 16;
    memory[registers[8] + 3] = registers[instr.first_register] >> 24;
    break;

  case popl:
    buffer = ((unsigned int)memory[registers[8] + 3]) << 24;
    buffer += ((unsigned int)memory[registers[8] + 2]) << 16;
    buffer += ((unsigned int)memory[registers[8] + 1]) << 8;
    buffer += ((unsigned int)memory[registers[8]]);
    registers[instr.first_register] = buffer;
    registers[8] += 4;
    break;
    
  case printr:
    printf("%d (0x%x)\n", registers[instr.first_register], registers[instr.first_register]);
    break;
    
  case readr:
    scanf("%d", &(registers[instr.first_register]));
    break;
  }

  // program_counter + 4 represents the subsequent instruction
  return program_counter + 4;
}

/*
 * Returns the file size in bytes of the file referred to by the given descriptor
*/
unsigned int get_file_size(int file_descriptor)
{
  struct stat file_stat;
  fstat(file_descriptor, &file_stat);
  return file_stat.st_size;
}

/*
 * Loads the raw bytes of a file into an array of 4-byte units
*/
unsigned int* load_file(int file_descriptor, unsigned int size)
{
  unsigned int* raw_instruction_bytes = (unsigned int*)malloc(size);
  if(raw_instruction_bytes == NULL)
    error_exit("unable to allocate memory for instruction bytes (something went really wrong)");

  int num_read = read(file_descriptor, raw_instruction_bytes, size);

  if(num_read != size)
    error_exit("unable to read file (something went really wrong)");

  return raw_instruction_bytes;
}

/*
 * Prints the opcode, register IDs, and immediate of every instruction, 
 * assuming they have been decoded into the instructions array
*/
void print_instructions(instruction_t* instructions, unsigned int num_instructions)
{
  printf("instructions: \n");
  unsigned int i;
  for(i = 0; i < num_instructions; i++)
  {
    printf("op: %d, reg1: %d, reg2: %d, imm: %d\n", 
	   instructions[i].opcode,
	   instructions[i].first_register,
	   instructions[i].second_register,
	   instructions[i].immediate);
  }
  printf("--------------\n");
}


/*
 * Prints an error and then exits the program with status 1
*/
void error_exit(const char* message)
{
  printf("Error: %s\n", message);
  exit(1);
}
