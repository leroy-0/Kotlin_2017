NAME = koak

all: $(NAME)

$(NAME): 
	gradle build
	echo 'java -jar build/libs/koak.jar $$1' > $(NAME)
	chmod +x $(NAME)

clean:
	gradle clean

fclean: clean 
	rm $(NAME)

re: fclean all

.PHONY: all clean fclean re
