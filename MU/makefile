SRC_DIR := src
BUILD_DIR := build
DEPS_DIR := .deps

COMMON_SRC := low_mem_graph.cpp local_search.cpp \
 union_find.cpp instance.cpp multi_cc_instance.cpp
COMMON_SRC := $(addprefix $(SRC_DIR)/, $(COMMON_SRC))
COMMON_OBJ := $(patsubst $(SRC_DIR)/%.cpp, $(BUILD_DIR)/%.o, $(COMMON_SRC))

CXXFLAGS := -std=c++17 -Wall -Wextra -Ofast -flto -DNDEBUG

INC := -I$(SRC_DIR)

.PHONY: all
all: main

main: $(BUILD_DIR)/main.o $(COMMON_OBJ)
	$(CXX) $(CXXFLAGS) -o $@ $^ -static 

main.tgz: main
	tar -cvzf $@ $<

$(BUILD_DIR):
	mkdir -p $@
$(DEPS_DIR):
	mkdir -p $@

$(BUILD_DIR)/%.o: $(SRC_DIR)/%.cpp | $(BUILD_DIR) $(DEPS_DIR)
	$(CXX) $(CXXFLAGS) $(INC) -MMD -MF $(DEPS_DIR)/$*.d -o $@ -c $<

.PHONY: clean
clean:
	$(RM) main main.tgz $(BUILD_DIR)/*.o $(DEPS_DIR)/*.d

-include $(DEPS_DIR)/*.d
