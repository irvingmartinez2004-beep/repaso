#Librerias implementadas
library(MASS)
library(neuralnet)
library(boot)
library(plyr)

#Asignación de los datos
data <- Boston  

#Asignación de la semilla 
set.seed(500) 

#Verificar que no falte ningún punto de datos
apply(data,2,function(x) sum(is.na(x)))

#Dividir aleatoriamente los datos en un entrenamiento y un conjunto de prueba
index <- sample(1:nrow(data), round(0.75 * nrow(data))) 
train <- data[index, ] 
test <- data[-index, ]  

#Ajustar el modelo de regresión lineal
lm.fit <- glm(medv ~ ., data = train) 
summary(lm.fit)  

#Predecir valores en el conjunto de prueba
pr.lm <- predict(lm.fit, newdata = test)  

#Calcular el error cuadrático medio (MSE)
MSE.lm <- sum((pr.lm - test$medv)^2) / nrow(test)  
print(paste("MSE del modelo lineal:", round(MSE.lm, 4))) 

#Preparación para la red neural
#Escalación de datos
maxs <- apply(data, 2, max)  
mins <- apply(data, 2, min)  

scaled <- as.data.frame(scale(data, center = mins, scale = maxs - mins))

#Dividir los datos escalados en entrenamiento y prueba
train_ <- scaled[index, ] 
test_ <- scaled[-index, ]

#Ajuste de la red 
n <- names(train_) 
f <- as.formula(paste("medv ~", paste(n[!n %in% "medv"], collapse = " + ")))
nn <- neuralnet(f,data=train_,hidden=c(5,3),linear.output=T)

#Representación grafica 
plot(nn)

#Predicción de medv usando la red neuronal
pr.nn <- compute(nn,test_[,1:13])  
pr.nn_ <- pr.nn$net.result*(max(data$medv) - min(data$medv))+min(data$medv) 
test.r <- (test_$medv)*(max(data$medv) - min(data$medv))+min(data$medv)   
MSE.nn <- sum((test.r - pr.nn_)^2)/nrow(test_)  

#Resultado obtenido
print(paste(MSE.lm,MSE.nn))  

#Validación cruzada   
par(mfrow=c(1,2)) 

# Gráfico para la red neuronal
plot(test$medv, pr.nn_, 
     col = 'red', 
     main = 'Real vs Predicted NN', 
     pch = 18, cex = 0.7) 
abline(0, 1, lwd = 2) 
legend('bottomright', legend = 'NN', pch = 18, col = 'red', bty = 'n') 

# Gráfico para la regresión lineal
plot(test$medv, pr.lm, 
     col = 'blue', 
     main = 'Real vs Predicted LM', 
     pch = 18, cex = 0.7) 
abline(0, 1, lwd = 2) 
legend('bottomright', legend = 'LM', pch = 18, col = 'blue', bty = 'n', cex = 0.95)

#Predección Ideal
plot(test$medv, pr.nn_, col='red', main='Real vs predicted NN', pch=18, cex=0.7)
points(test$medv, pr.lm, col='blue', pch=18, cex=0.7)
abline(0,1,lwd=2)
legend('bottomright', legend=c('NN', 'LM'), pch=18, col=c('red', 'blue'))

#Validación cruzada con bucle for
set.seed(200)
lm.fit <- glm(medv~.,data=data)
cv.glm(data,lm.fit,K=10)$delta[1]  

#Estado del proceso
set.seed(450)
cv.error <- NULL
k <- 10
pbar <- create_progress_bar('text')
pbar$init(k)
for(i in 1:k){
  index <- sample(1:nrow(data), round(0.9*nrow(data)))
  train.cv <- scaled[index,]
  test.cv <- scaled[-index,]
  f <- as.formula(paste("medv ~", paste(names(train.cv)[!names(train.cv) %in% "medv"], collapse = " + ")))
  nn <- neuralnet(f, data = train.cv, hidden = c(5,2), linear.output = T)
  pr.nn <- compute(nn, test.cv[,1:13])
  pr.nn <- pr.nn$net.result * (max(data$medv) - min(data$medv)) + min(data$medv)
  test.cv.r <- (test.cv$medv) * (max(data$medv) - min(data$medv)) + min(data$medv)
  cv.error[i] <- sum((test.cv.r - pr.nn)^2) / nrow(test.cv)
  pbar$step()
}

#Resultados obtenidos
mean(cv.error)
cv.error

#Diagrama de caja
boxplot(cv.error,xlab='MSE CV', col='cyan',
        border='blue',names='CV error (MSE)',
        main='CV error (MSE) for NN', horizontal=TRUE)