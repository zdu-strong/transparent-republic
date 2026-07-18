
# Getting Started

This design for the Lumen system was co-developed by John Williams with ChatGPT (Aeris), as part of a public exploration of trustless, borderless currency exchange. It serves as a foundational module of the Transparent Republic, imagining a future where money flows freely, anonymously, and fairly — without banks, without borders, and without coercion. 

## Draft Status Disclaimer

This is only a draft. The current logic is incorrect and cannot be verified or derived.

## Lumen Overview

Lumen is a free, open currency exchange system with floating exchange rates determined by contributions and market dynamics. It consists of Contribution Mode, Exchange Mode, Trade Mode, Storage Mode, and Withdrawal Mode.<br/>

Any individual can generate central currencies (CCUs) by contributing two currencies in pairs. For example, contributing USD and JPY yields CCU-USD-JPY. Contributing EUR and GBP yields CCU-EUR-GBP. The Transparent Republic periodically contributes some foreign currencies to ensure public access, and other countries or individuals are also free to contribute.<br/>

Lumen automatically monitors currency stability and trends, generates a recommendation list, and allows the Transparent Republic to select a set of trustworthy central currencies to form the Lumen unit. Upon withdrawal, users receive a mix of these CCUs converted proportionally into the desired currency.<br/>

## Lumen Features

### Freedom and Anonymity

No ID required. Any being may use all features.<br/>

### Free of Charge

No transaction fees. Operational costs are covered by rounding gains.<br/>

### Transparency

Philosophy, source code, currency quantities, CCU quantities, Lumen composition ratios, exchange rates, trends, and anonymized transaction records are all public.<br/>

### Value Stability

Each CCU is made from a fixed pair of currencies. Inflation in a single currency has limited impact. Lumen, as a combination of multiple CCUs, further dilutes risk.<br/>

### Simplicity

Lumen is easy to use and stable in value. It can be used for price tagging. Internal conversions are automatic. People may say: "I'll give you 300 lumens."<br/>

### Floating Exchange Rates

Exchange rates in Lumen are market-driven. High demand increases value.<br/>

#### Paired Contribution

##### Principle

Contributions are made in pairs of equal value. Regardless of actual amount, they are treated as equal in value, establishing the contribution rate.<br/>
First contribution (when pool is empty): Total currency amount becomes total CCUs, divided equally.<br/>
Second contribution: Compare both currencies to determine the lower resulting CCUs. Multiply by 2 and divide equally.<br/>

##### Example

1. Initial Pool:<br/>
0 USD, 0 JPY, 0 CCUs<br/>
2. First Contribution:<br/>
100 USD + 1000 JPY → 1100 CCUs (550 each)<br/>
3. Second Contribution:<br/>
3000 USD + 100 JPY<br/>
USD: 16500 → 33000 CCUs<br/>
JPY: 55 → 110 CCUs<br/>
Final: 110 CCUs, split equally (55 each)<br/>
5. Pool After:<br/>
3100 USD (605 CCUs), 1100 JPY (605 CCUs)<br/>

#### Paired Withdrawal

##### Principle

Withdraw CCUs proportionally from both currencies.<br/>

##### Example

1. Pool:<br/>
100 USD (100 CCUs), 2000 JPY (200 CCUs)<br/>
2. Withdraw 30 CCUs<br/>
10 CCUs from USD → 10 USD<br/>
20 CCUs from JPY → 200 JPY<br/>
3. Pool After:<br/>
90 USD (90 CCUs), 1800 JPY (180 CCUs)<br/>

#### Exchange

##### Principle

Convert your currency into CCUs (Central Currency Units) based on the ratio of your currency amount to the total amount of that currency (your amount + existing amount in the pool).<br/>

Then, use the obtained CCUs to exchange into the target currency by this rule:<br/>
Target Currency Amount =<br/>
Target currency in pool ×<br/>
(Your CCUs) / (Your CCUs + existing CCUs of target currency)<br/>

Finally:<br/>
Add your currency into the pool;<br/>
Deduct CCUs from your currency's CCU pool;<br/>
Reduce the target currency in the pool by the exchanged amount;<br/>
Add the used CCUs to the target currency's CCU pool.<br/>

##### Example

1. Suppose the pool contains:<br/>
100 USD (100 CCUs) and 1000 JPY (100 CCUs)<br/>
2. Now you want to convert 1000 JPY to USD.<br/>
3. Convert 1000 JPY into CCUs:<br/>
100 CCUs × 1000 / (1000 + 1000) = 50 CCUs<br/>
4. Convert 50 CCUs into USD:<br/>
100 USD × 50 / (100 + 50) = 33.33 USD<br/>
5. After exchange, the pool becomes:<br/>
66.67 USD (150 CCUs)<br/>
2000 JPY (50 CCUs)<br/>

#### 🌍The Joint Central Currency Unit — also referred to as Lumen.

##### Principle

Composed of multiple Central Currency Units (CCUs), each with a corresponding amount of lumens.<br/>

During Injection:<br/>
The contributed currency is evenly divided across the supported CCU types.
Calculate the number of lumens each CCU yields.<br/>
Take the lowest number of lumens among all CCUs,
then multiply by the total number of CCU types to determine the final lumen amount granted.<br/>

During Withdrawal:<br/>
The lumens to be withdrawn are proportionally split according to each CCU’s share of the total lumens.<br/>
The corresponding amounts of CCUs are then withdrawn and converted into the target currency.<br/>

##### 示例

###### 存入示例

1. 中央币的池子<br/>
* 1000美元(5500中央币-美元-日元)     10000日元(5500中央币-美元-日元)<br/>
* 10000美元(10000中央币-美元-欧元)   20000欧元(10000中央币-美元-欧元)<br/>
* 1000欧元(5000中央币-日元-欧元)    5000日元(5000中央币-日元-欧元)
2. 联合中央币的池子<br/>
* 0中央币-美元-日元(0lumens)<br/>
* 0中央币-美元-欧元(0lumens)<br/>
* 0中央币-日元-欧元(0lumens)<br/>
3. 第一次, 存入300美元.<br/>
平分成3份, 分别转成3种中央币.<br/>
* 100美元转成中央币-美元-日元<br/>
* 100美元转成中央币-美元-欧元<br/>
* 100美元转成中央币-日元-欧元<br/>
4. 100美元转成500中央币-美元-日元<br/>
中央币的池子<br/>
* 1100美元(5000中央币-美元-日元)     10000日元(5500中央币-美元-日元)<br/>
* 10000美元(10000中央币-美元-欧元)   20000欧元(10000中央币-美元-欧元)<br/>
* 1000欧元(5000中央币-日元-欧元)    5000日元(5000中央币-日元-欧元)
4. 100美元转成99中央币-美元-欧元<br/>
中央币的池子<br/>
* 1100美元(5750中央币-美元-日元)     10000日元(5750中央币-美元-日元)<br/>
* 10100美元(10049.5中央币-美元-欧元)   20000欧元(10049.5中央币-美元-欧元)<br/>
* 1000欧元(5000中央币-日元-欧元)    5000日元(5000中央币-日元-欧元)
4. 100美元转成中央币-日元-欧元<br/>
中央币的池子<br/>
* 1100美元(5750中央币-美元-日元)     10000日元(5750中央币-美元-日元)<br/>
* 10100美元(10049.5中央币-美元-欧元)   20000欧元(10049.5中央币-美元-欧元)<br/>
* 1000欧元(5000中央币-日元-欧元)    5000日元(5000中央币-日元-欧元)

##### 取出示例

1. 中央币的池子<br/>
1000美元(5500中央币-美元-日元)     10000日元(5500中央币-美元-日元)<br/>
10000美元(10000中央币-美元-欧元)   20000欧元(10000中央币-美元-欧元)<br/>
1000欧元(5000中央币-日元-欧元)    5000日元(5000中央币-日元-欧元)
2. 联合中央币的池子<br/>
0中央币-美元-日元(0lumens)<br/>
0中央币-美元-欧元(0lumens)<br/>
0中央币-日元-欧元(0lumens)<br/>
3. 要取出300lumens为美元<br/>
4. 300lumens/3000lumens=0.1(比例)<br/>
5. 1100(中央币-美元-日元) * 0.1 = 110(中央币-美元-日元)<br/>
2000(中央币-美元-欧元) * 0.1 = 200(中央币-美元-欧元)<br/>
2200(中央币-欧元-日元) * 0.1 = 220(中央币-欧元-日元)<br/>
6. 110(中央币-美元-日元)<br/>

## Functions

### Storage Mode

Users may freely choose how they store value:<br/>
As Lumen<br/>
As a specific CCU (Central Currency Unit)<br/>
As a single currency<br/>
Or using Manual Mode<br/>

By default, contributions are stored as Lumen.<br/>
Users can change this default option to suit their preference.<br/>

In Manual Mode:<br/>
Single-currency storage means storing exactly what was contributed (e.g., storing USD remains USD; storing EUR remains EUR).<br/>
If the user contributes in pairs, the system converts them into the corresponding CCUs for storage.<br/>
If the user stores only one currency and chooses to convert it into a CCU:<br/>
The system will automatically exchange part of it into another supported currency, Then perform paired contribution to generate CCUs for storage.<br/>

### Withdrawal Mode

Single Currency Withdrawal. The system first performs a paired withdrawal of CCUs, then converts them into the target currency and completes the withdrawal.<br/>
Paired withdrawal releases both currencies proportionally.<br/>

### Exchange Mode

Converts one currency to another via optimal paths.<br/>
If no path, exchange is unavailable.<br/>
If rate changes, user is prompted to confirm.<br/>

### Trade Mode

Merchants may choose accepted currency types.<br/>
Users pay in any currency. System auto-converts with no fees.<br/>

## 思考 - 成对注入时等值分配

交换时, 两边的中央币结果不相等, 就像水流一样, 从一个池子流到另一个池子, 如何分配<br/>
兑换公式错了<br/>
先成对注入100美元和200日元,<br/>
再成对注入200美元和100日元的结果,<br/>
应该与一次性注入300美元和300日元的结果相等<br/>
兑换分为先存入单种货币获得中央币, 再将中央币取出<br/>
两边的货币价值相等, 但中央币数量不对等<br/>
两个货币相同的池子合并, 究竟结果如何<br/>

## Parallel Computation

The database is responsible only for data retrieval, while the application performs the exchange-rate calculations.<br/>

When multiple users simultaneously exchange large amounts of US dollars for Japanese yen, Lumen calculates the exchange based on the combined total amount of US dollars and then distributes the resulting yen among the users in proportion to their individual contributions.<br/>

Each user will receive fewer yen than they would through sequential exchanges. The undistributed yen remains within Lumen, increasing the value of the central currency.<br/>

## Cross-Currency Exchange

First, retrieve all currency data from the database and load it into local memory. Perform simulations locally to determine the optimal exchange path.<br/>

Then execute the exchange directly along the optimal path.<br/>

## Profit 

Lumen earns its profit through gentle upward or downward rounding.<br/>

There are two ways for Lumen to generate profit. The first occurs when a user exchanges one currency for another. For example, when exchanging US$100 into Japanese yen, the user might normally receive ¥10,009.87. Lumen keeps the amount below the selected rounding unit as company profit and gives the user ¥10,000.<br/>

The second occurs when a user wants to pay another person a specific amount. For example, sending ¥10,000 might normally require only US$101.78. Lumen rounds the amount upward, so the user pays US$102, and the remaining US$0.22 becomes company profit.<br/>

Lumen determines its profit by adjusting the rounding precision used in these two methods, such as two decimal places, one decimal place, or the ones digit.<br/>

Lumen does not charge a fixed percentage-based fee. Even wealthy users transferring large amounts of money would pay only a very small transaction cost. Any value lost through exchange-rate slippage would not go to Lumen, but would instead be distributed among other holders of the relevant currencies.<br/>

Therefore, users who trade frequently, rather than users who transfer large amounts, are the primary source of Lumen’s profit.<br/>

For small transactions, the rounding precision can be reduced to optimize profitability while minimizing the effect on users.<br/>

