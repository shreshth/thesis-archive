
#[1] "lat"          "lng"          "bearing"      "speed"        "user_id"     
#[6] "time"         "wday"         "time_to_wifi"

regress <- function(data) {
  # constants
  lat_max <- 40.3530;
  lat_min <- 40.3390;
  lat_range <- lat_max - lat_min
  lng_min <- -74.6660;
  lng_max <- -74.6440;
  lng_range <- lng_max - lng_min

  # parameters
  bearing_div <- 4;
  lat_div <- 4;
  lng_div <- 4;
  lambda <- seq(1,4, 0.4)
  num_folds <- 5

  # prediction distance
  pred_dist <- matrix(0,length(lambda),1)

  #count<-0

  folds <- sample(1:num_folds, nrow(data), replace=TRUE)
  for (k in 1:num_folds) {
    # fold indices
    in_fold_index <- which(folds == k)
    out_fold_index <- which(folds != k)

    # fold data
    data_in_fold <- data[in_fold_index,]
    data_out_fold <- data[out_fold_index,]

    # loop over all bearing divisions
    for (x in 1: bearing_div) {
      # bearing range in this division
      bearing_min_div <- (360/bearing_div) * (x-1)
      bearing_max_div <- (360/bearing_div) * x
      # loop over all latitude divisions
      for (y in 1: lat_div) {
	# latitude range in this division
	lat_min_div <- lat_min + ((lat_range/lat_div) * (y-1))
	lat_max_div <- lat_min + ((lat_range/lat_div) * y)
	# loop over all longitude divisions
	for (z in 1: lng_div) {
	  # longitude range in this division
	  lng_min_div <- lng_min + ((lng_range/lng_div) * (z-1))
	  lng_max_div <- lng_min + ((lng_range/lng_div) * z)

	  # out of fold data in this lat/lng/bearing range
	  indices_div_out_fold <- rownames(data_out_fold)[which(data_out_fold$bearing >= bearing_min_div &
						   data_out_fold$bearing <  bearing_max_div &
						   data_out_fold$lat     >= lat_min_div     &
						   data_out_fold$lat     <  lat_max_div     &
						   data_out_fold$lng     >= lng_min_div     &
						   data_out_fold$lng     <  lng_max_div, arr.ind=TRUE)]
	  
	  # in fold data in this lat/lng/bearing range
	  indices_div_in_fold <- rownames(data_in_fold)[which(data_in_fold$bearing >= bearing_min_div &
						       data_in_fold$bearing <  bearing_max_div &
						       data_in_fold$lat     >= lat_min_div     &
						       data_in_fold$lat     <  lat_max_div     &
						       data_in_fold$lng     >= lng_min_div     &
						       data_in_fold$lng     <  lng_max_div, arr.ind=TRUE)]

	  #print(indices_div_out_fold)
	  #print(indices_div_in_fold)

	  if (length(indices_div_out_fold) > 0) {
	    # train model on out of fold data in this lat/lng/bearing division
	    model <- lm.ridge(time_to_wifi ~ speed+time+wday, data=data_out_fold[indices_div_out_fold,], lambda=lambda)

	    # coefficients of ridge model
	    coeff <- coef(model)

	    #print(coeff)
	    #print(coeff[,'speed'])
	    # compute cumulative L1 distance
	    for (i in 1:length(lambda)) { # for all values of lambda
	      for (j in 1:length(indices_div_in_fold)) { # for all in fold data in the lat/lng/bearing division
		#count<-count+1
		pred_val <- coeff[i, 1] + (coeff[i, 'speed'] * data_in_fold[indices_div_in_fold[j], 'speed']) + (coeff[i, 'time'] * data_in_fold[indices_div_in_fold[j], 'time']) + (coeff[i, 'wday'] * data_in_fold[indices_div_in_fold[j], 'wday'])
		pred_dist[i] = pred_dist[i] + abs(pred_val - data_in_fold[indices_div_in_fold[j], 'time_to_wifi'])
		#print(paste("A:", pred_val))
		#print(paste("B:", data_in_fold[indices_div_in_fold[j], 'time_to_wifi']))
		#print(paste("C:", i, pred_dist[i]))
	      }
	    }
	  }
	}
      }
    }
  }
  #print(pred_dist)
  pred_dist = pred_dist/nrow(data)
  print(pred_dist)
  #print(count)
}

require(MASS)
input_file <- "prepared_data.txt";
data <- read.table(input_file)
regress(data)