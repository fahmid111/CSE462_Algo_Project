BUILD_DIR := ./build
OUT_DIR := .
SOURCE_DIR := ./src
SOLVER_NAME := ade-ce-solver
MAIN_CLASS := de.umr.pace.clusterediting.exact.ClusterEditingSolver

main:
	mkdir -p ${BUILD_DIR}
	mkdir -p ${OUT_DIR}
	find ${SOURCE_DIR} -name "*.java" > ${BUILD_DIR}/sources.list
	javac -d ${BUILD_DIR} @${BUILD_DIR}/sources.list
	(cd ${BUILD_DIR} && find . -name "*.class" > classes.list)
	(cd ${BUILD_DIR} && jar --create --file ${SOLVER_NAME}.jar --main-class ${MAIN_CLASS} @classes.list)
	mv ${BUILD_DIR}/${SOLVER_NAME}.jar ${OUT_DIR}/${SOLVER_NAME}.jar
	rm ${BUILD_DIR}/sources.list ${BUILD_DIR}/classes.list

clean:
	rm -f -r ${BUILD_DIR}