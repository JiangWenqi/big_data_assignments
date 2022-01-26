library(shiny)
library(shinyWidgets)
library(tidyverse)
library(ggplot2)
library(plotly)
library(streamgraph)

#Establecemos nuestro directorio de trabajo
getwd()
#setwd('C:\\UPM\\BookingShinyApp')

df = read.csv('data/hotel_bookings.csv')

#convert to factor (categorical)
df$hotel=as.factor(df$hotel)
df$is_canceled=as.factor(df$is_canceled)

#We create new quantitative variables
df$guests <- df$adults + df$children + df$babies
df$nights <- df$stays_in_weekend_nights + df$stays_in_week_nights

#Some filters for missing values
#Reservation with guests
df <- filter(df, guests > 0)
#Reservation with nights
df <- filter(df, nights > 0)
df <- filter(df, distribution_channel %in% c("Corporate", "Direct", "GDS", "TA/TO"))

years <- unique(df$arrival_date_year)
months <- unique(df$arrival_date_month)

#HEATMAP DATASET
df.heatmap <- subset(df, select= c(hotel, arrival_date_year, arrival_date_month, arrival_date_day_of_month, is_canceled, guests, nights))
#We rename the variables
colnames(df.heatmap)=c("hotel","Year","Month","Day","is_canceled","guests","nights")
df.heatmap$Month=factor(df.heatmap$Month, levels = c("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" ))
df.heatmap$Day=as.factor(df.heatmap$Day)

#STREAMMAP DATASET
df.trend <- subset(df, select= c(hotel, arrival_date_year, arrival_date_month, arrival_date_day_of_month, is_canceled, guests, nights,
                                 market_segment, distribution_channel, customer_type))
colnames(df.trend)=c("hotel","Year","Month","Day","is_canceled","guests","nights", "market", "distribution", "customer")
df.trend$market=as.factor(df.trend$market)
df.trend$distribution=as.factor(df.trend$distribution)
df.trend$customer=as.factor(df.trend$customer)


# SCATTERPLOT
df.scatter <-
  subset(
    df,
    select = c(
      hotel,
      arrival_date_year,
      arrival_date_month,
      arrival_date_day_of_month,
      is_canceled,
      guests,
      nights,
      lead_time,
      reservation_status_date
    )
  )
colnames(df.scatter) = c(
  "hotel",
  "Year",
  "Month",
  "Day",
  "is_canceled",
  "guests",
  "nights",
  "lead_time",
  "status_date"
)

  # Define UI ----
ui <- navbarPage(
  "Hotel booking demand App",
  tabPanel("Introduction",
           sidebarLayout(
             sidebarPanel(
               h2("Hotel booking demand app"),
               p("The main goal of this app is to help an analyst to find some insights about the demand of hotels."),
               p("The analyst could be the regional manager of a major hotel chain that uses reservation information to make the best decisions regarding adjustments to its facilities, marketing strategies to attract more guests, and everything else needed to offer the best possible experience to its guests."),
               br(),
               br("This app was developed by"),
               p(a("Alvaro Ramirez", 
                   href = "https://github.com/alvaroame")),
               p(a("Wenqi Jiang", 
                   href = "https://github.com/JiangWenqi")),
               p("Zhuo Cheng"),
               br("Powered by"),
               "Shiny, a product of ", 
               span("RStudio", style = "color:blue")
             ),
             mainPanel(
               h1("Introducing Hotel booking demand dataset"),
               p("The dataset represents information regarding the demand for two types of hotels, one is a resort-type hotel and the other is an urban hotel, it contains 32 attributes that include information such as when the reservation was made, length of stay, number of adults , children and/or babies, the number of available parking spaces, among other things. and approximately 119,000 observations where each observation represents a hotel reservation from July 1, 2015 to August 31, 2017, including reservations that actually arrived and those that were cancelled."),
               
               p(a("Dataset in Kaggle", 
                   href = "https://www.kaggle.com/jessemostipak/hotel-booking-demand")),
               br(),
               p('These visualizations aim to help the analyst to find answers to three different questions:'),
               p(strong('1.- How is demand distributed throughout the year?')),
               p(strong('2.- How is the trend on the customer type, market segment and distribution channels?')),
               p(strong('3.- Is there a correlation between reservation time and the total number of nights the guest stayed at the hotel?')),
               p(),
               br(),
             )
           )
  ),
  tabPanel("Demand distribution",
           h2("How is demand distributed throughout the year?"),
           p('This visualization aim to help the analyst to find the answer'),
           br(),
           plotlyOutput("heatPlot"),
           hr(),
           fluidRow(
             column(3,
                    selectInput("hotel", 
                                label = "Type of hotel",
                                choices = c("City Hotel", "Resort Hotel", "All"),
                                selected = "All"),
             ),
             column(3,
                    selectInput("year", 
                                label = "Year",
                                choices = c(years, "All"),
                                selected = "2016"),
             ),
             column(3,
                    selectInput("cancelled", 
                                label = "Cancellations",
                                choices = c("Cancelled", "Not Cancelled", "All"),
                                selected = "Not Cancelled"),
             ),
             column(3,
                    selectInput("score", 
                                label = "Variable to analyse",
                                choices = c("Guests", "Nights", "Reservations"),
                                selected = "Guests"),
             ),
           ),
  ),
  tabPanel("Historical Trend",
           h2("How is the trend on the customer type, market segment and distribution channels?"),
           p('This visualization aim to help the analyst to find the answer'),
           br(),
           streamgraphOutput("streamMap"),
           hr(),
           fluidRow(
             column(3,
                    selectInput("hotel2", 
                                label = "Type of hotel",
                                choices = c("City Hotel", "Resort Hotel", "All"),
                                selected = "All"),
             ),
             column(3,
                    selectInput("year2", 
                                label = "Year",
                                choices = c(years, "All"),
                                selected = "2016"),
                    selectInput("month", 
                                label = "Month",
                                choices = c("January",
                                            "February",
                                            "March",
                                            "April",
                                            "May",
                                            "June",
                                            "July",
                                            "August",
                                            "September",
                                            "October",
                                            "November",
                                            "December",
                                            "All"),
                                selected = "All"),
             ),
             column(3,
                    selectInput("cancelled2", 
                                label = "Cancellations",
                                choices = c("Cancelled", "Not Cancelled", "All"),
                                selected = "Not Cancelled"),
             ),
             column(3,
                    selectInput("category", 
                                label = "Category",
                                choices = c("Customer", "Origin", "Market"),
                                selected = "Origin"),
             ),
           ),
  ),
  tabPanel("Correlations",
           h2("Is there a correlation between reservation time and the total number of nights the guest stayed at the hotel?"),
           p('This visualization aim to help the analyst to find the answer'),
           
           plotlyOutput("scatterplot"),
           hr(),
           fluidRow(
             column(3,
                    selectInput("hotel3", 
                                label = "Type of hotel",
                                choices = c("City Hotel", "Resort Hotel", "All"),
                                selected = "Resort Hotel"),
             ),
             column(3,
                    selectInput("year3", 
                                label = "Year",
                                choices = c(years, "All"),
                                selected = "2016"),
             ),
             column(3,
                    selectInput("month2", 
                                label = "Month",
                                choices = c("January",
                                            "February",
                                            "March",
                                            "April",
                                            "May",
                                            "June",
                                            "July",
                                            "August",
                                            "September",
                                            "October",
                                            "November",
                                            "December",
                                            "All"),
                                selected = "August"),
             ),
             column(3,
                    selectInput("cancelled3", 
                                label = "Cancellations",
                                choices = c("Cancelled", "Not Cancelled", "All"),
                                selected = "Not Cancelled"),
             ),
           ),
  )
  )
  # Define server logic ----
  server <- function(input, output, session) {
    
    getScatterData <- reactive({
      #for hotel
      dataScatter <- switch(
        input$hotel3,
        "City Hotel" = filter(df.scatter, hotel == "City Hotel"),
        "Resort Hotel" = filter(df.scatter, hotel == "Resort Hotel"),
        "All" = df.scatter
      )
      
      #for one year
      dataScatter <- switch(
        input$year3,
        "2015" = filter(dataScatter, Year == 2015),
        "2016" = filter(dataScatter, Year == 2016),
        "2017" = filter(dataScatter, Year == 2017),
        "All" =  dataScatter
      )
      
      #not cancelled
      dataScatter <- switch(input$cancelled3, 
                              "Cancelled" = filter(dataScatter, is_canceled == 1),
                              "Not Cancelled" = filter(dataScatter, is_canceled == 0),
                              "All" = dataScatter)
      
      
      dataScatter <- switch(
        input$month2,
        "January" = filter(dataScatter, Month == "January"),
        "February" = filter(dataScatter, Month == "February"),
        "March" = filter(dataScatter, Month == "March"),
        "April" = filter(dataScatter, Month == "April"),
        "May" = filter(dataScatter, Month == "May"),
        "June" = filter(dataScatter, Month == "June"),
        "July" = filter(dataScatter, Month == "July"),
        "August" = filter(dataScatter, Month == "August"),
        "September" = filter(dataScatter, Month == "September"),
        "October" = filter(dataScatter, Month == "October"),
        "November" = filter(dataScatter, Month == "November"),
        "December" = filter(dataScatter, Month == "December"),
        "All" = dataScatter
      )
      
      dataScatter
    })
    
    output$scatterplot <- renderPlotly({
      scatterData <- getScatterData()
      #scatterPlot <- ggplot(scatterData, aes(x = nights, y = lead_time, color=hotel)) +
      #  geom_point(size = scatterData$guests) +
      #  theme_ipsum()
      # plotly <- ggplotly(scatterPlot)
      
      fig <- plot_ly(
        scatterData, x = ~lead_time, y = ~nights,
        text = ~paste("Guests: ", guests,
                      '<br>Date:', paste(Day, Month, Year, sep="-")),
        color = ~hotel, size = ~guests
      )
      fig
     
    })
    
    getStreamMapData <- reactive({
      #for hotel
      dataStreamMap <- switch(input$hotel2, 
                            "City Hotel" = filter(df.trend, hotel == "City Hotel"),
                            "Resort Hotel" = filter(df.trend, hotel == "Resort Hotel"),
                            "All" = df.trend)
      
      #for one year
      dataStreamMap <- switch(input$year2, 
                            "2015" = filter(dataStreamMap, Year == 2015),
                            "2016" = filter(dataStreamMap, Year == 2016),
                            "2017" = filter(dataStreamMap, Year == 2017),
                            "All" = dataStreamMap)
      
      #for one month
      dataStreamMap <- switch(input$month, 
                              "January" = filter(dataStreamMap, Month == "January"),
                              "February" = filter(dataStreamMap, Month == "February"),
                              "March" = filter(dataStreamMap, Month == "March"),
                              "April" = filter(dataStreamMap, Month == "April"),
                              "May" = filter(dataStreamMap, Month == "May"),
                              "June" = filter(dataStreamMap, Month == "June"),
                              "July" = filter(dataStreamMap, Month == "July"),
                              "August" = filter(dataStreamMap, Month == "August"),
                              "September" = filter(dataStreamMap, Month == "September"),
                              "October" = filter(dataStreamMap, Month == "October"),
                              "November" = filter(dataStreamMap, Month == "November"),
                              "December" = filter(dataStreamMap, Month == "December"),
                              "All" = dataStreamMap)
      
      #not cancelled
      dataStreamMap <- switch(input$cancelled2, 
                            "Cancelled" = filter(dataStreamMap, is_canceled == 1),
                            "Not Cancelled" = filter(dataStreamMap, is_canceled == 0),
                            "All" = dataStreamMap)
      
      #Category
      cat_selected <- switch(input$category, 
                              "Customer" = 'customer',
                              "Origin" = 'distribution',
                              "Market" = 'market')
      
      if (input$month == "All"){
        dataStreamMap.totals <- dataStreamMap %>% 
          group_by_('Year', 'Month', cat_selected) %>% 
          summarise(count=n())
        dataStreamMap.totals$date <- paste(dataStreamMap.totals$Year, 
                                           dataStreamMap.totals$Month,
                                           1,
                                           sep="-")
      }else {
        dataStreamMap.totals <- dataStreamMap %>% 
          group_by_('Year', 'Month', 'Day', cat_selected) %>% 
          summarise(count=n())
        dataStreamMap.totals$date <- paste(dataStreamMap.totals$Year, 
                                           dataStreamMap.totals$Month,
                                           dataStreamMap.totals$Day,
                                           sep="-")
      }
      
      dataStreamMap.totals$date <- parse_date(dataStreamMap.totals$date,  "%Y-%B-%d", locale = locale("en"))
      dataStreamMap.totals
      
    })
    
    # StreamMap 
    output$streamMap <- renderStreamgraph({
      dataStreamMap <- getStreamMapData()
      
      #we plot the streamMap
      if (input$month == "All"){
        
        if (input$category == "Customer"){
          plot <- streamgraph(dataStreamMap, key=customer, value="count", date="date", height="300px", width="1100px") %>%
            sg_axis_x(1, "month", "%b, %Y") %>%
            sg_legend(show=TRUE, label="names: ")
        }else if (input$category == "Origin"){
          plot <- streamgraph(dataStreamMap, key=distribution, value="count", date="date", height="300px", width="1100px") %>%
            sg_axis_x(1, "month", "%b, %Y") %>%
            sg_legend(show=TRUE, label="names: ")
        } else if (input$category == "Market"){
          plot <- streamgraph(dataStreamMap, key=market, value="count", date="date", height="300px", width="1100px") %>%
            sg_axis_x(1, "month", "%b, %Y") %>%
            sg_legend(show=TRUE, label="names: ")
        }
      }else {
        if (input$category == "Customer"){
          plot <- streamgraph(dataStreamMap, key=customer, value="count", date="date", height="300px", width="1100px") %>%
            sg_axis_x(1, "day", "%d") %>%
            sg_legend(show=TRUE, label="names: ")
        } else if (input$category == "Origin"){
          plot <- streamgraph(dataStreamMap, key=distribution, value="count", date="date", height="300px", width="1100px") %>%
            sg_axis_x(1, "day", "%d") %>%
            sg_legend(show=TRUE, label="names: ")
        } else if (input$category == "Market"){
          plot <- streamgraph(dataStreamMap, key=market, value="count", date="date", height="300px", width="1100px") %>%
            sg_axis_x(1, "day", "%d") %>%
            sg_legend(show=TRUE, label="names: ")
        }
      }
    })
    
    getHeatMapData <- reactive({
      #for hotel
      dataHeatMap <- switch(input$hotel, 
                            "City Hotel" = filter(df.heatmap, hotel == "City Hotel"),
                            "Resort Hotel" = filter(df.heatmap, hotel == "Resort Hotel"),
                            "All" = df.heatmap)
      
      #for one year
      dataHeatMap <- switch(input$year, 
                            "2015" = filter(dataHeatMap, Year == 2015),
                            "2016" = filter(dataHeatMap, Year == 2016),
                            "2017" = filter(dataHeatMap, Year == 2017),
                            "All" = dataHeatMap)
      
      #not cancelled
      dataHeatMap <- switch(input$cancelled, 
                            "Cancelled" = filter(dataHeatMap, is_canceled == 1),
                            "Not Cancelled" = filter(dataHeatMap, is_canceled == 0),
                            "All" = dataHeatMap)
      
      
      print(input$hotel)
      print(input$year)
      print(input$cancelled)
      print(input$score)
      
      #we sum-up totals
      df.heatmap.totals <- dataHeatMap %>% 
        group_by(Month, Day) %>% 
        summarise(reservations = n(), guests = sum(guests), nights = sum(nights))
      
      #text for tooltip
      df.heatmap.totals <- df.heatmap.totals %>%
        mutate(text = paste0(Month, " ", Day, "\n",
                             "Reservations: ", reservations, "\n",
                             "Guests: ", guests, "\n", 
                             "Nights: ", nights, "\n"))
      
      df.heatmap.totals
    })
    
    # heatmap 
    output$heatPlot <- renderPlotly({
      dataHeatMap <- getHeatMapData()
      #we plot the heatmap
      heatMapPlot <- ggplot(dataHeatMap, aes_string(x = 'Day', y = 'Month', fill = tolower(input$score), text='text')) +
        geom_tile() + 
        scale_fill_gradient(low = "white", high = "steelblue") +
        theme_light() 
      plotly <- ggplotly(heatMapPlot, tooltip="text")
    })
    
  }
  
  # Run the app ----
  shinyApp(ui = ui, server = server)