import { observer } from "mobx-react-use-autorun";
import { BarChart } from '@mui/x-charts/BarChart';
import { Box, Chip, Typography } from "@mui/material";
import Income from "@component/CountryDashboard/Income";
import Outlay from "@component/CountryDashboard/Outlay";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCartShopping, faCircleDollarToSlot, faHouseChimney } from '@fortawesome/free-solid-svg-icons'

export default observer(() => {

    const datasetOfNationalReserves = [
        {
            london: 59,
            paris: 57,
            newYork: 86,
            seoul: 21,
            month: 'Jan',
        },
        {
            london: 50,
            paris: 52,
            newYork: 78,
            seoul: 28,
            month: 'Feb',
        },
        {
            london: 47,
            paris: 53,
            newYork: 106,
            seoul: 41,
            month: 'Mar',
        }];

    function valueFormatter(value: number | null) {
        return `${value}$`;
    }

    return <div className="flex flex-col">
        <div className="flex flex-row justify-between">
            <Box
                sx={{
                    width: '100%',
                    overflow: 'auto',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                }}
            >
                <Typography variant="h6">
                    {/* {"National Reserves comparison (2024 vs 2010-19 Avg)"} */}
                    {"National Reserves"}
                </Typography>
                <BarChart
                    dataset={datasetOfNationalReserves}
                    xAxis={[{ dataKey: 'month' }]}
                    // xAxis={[{ data: ['National Reserves'] }]}
                    series={[{ dataKey: 'london', label: 'National Reserves', valueFormatter }]}
                    height={300}
                />
            </Box>
            <Box
                sx={{
                    width: '100%',
                    overflow: 'auto',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                }}
            >
                <Typography variant="h6">
                    {/* {"Basic living allowance comparison (2024 vs 2010-19 Avg)"} */}
                    {"Basic living allowance"}
                </Typography>
                <BarChart
                    xAxis={[{ data: ['Basic living allowance'] }]}
                    series={[{ data: [4] }, { data: [1] }, { data: [6] }]}
                    height={300}
                />
            </Box>
            <Box
                sx={{
                    width: '100%',
                    overflow: 'auto',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                }}
            >
                <Typography variant="h6">
                    {/* {"Sovereign Dividend comparison (2024 vs 2010-19 Avg)"} */}
                    {"Sovereign Dividend"}
                </Typography>
                <BarChart
                    xAxis={[{ data: ['Sovereign Dividend'] }]}
                    series={[{ data: [4] }, { data: [1] }, { data: [6] }]}
                    height={300}
                />
            </Box>
        </div>
        <div className="flex flex-row" style={{ width: "1000px" }}>
            <Income />
            <Outlay />
            <div className="flex flex-col justify-center">
                <Chip icon={<FontAwesomeIcon icon={faCartShopping} />} label="Consumption tax: 10%" style={{ marginBottom: "1em" }} />
                <Chip icon={<FontAwesomeIcon icon={faCircleDollarToSlot} />} label="Profit tax: 40%" style={{ marginBottom: "1em" }} />
                <Chip icon={<FontAwesomeIcon icon={faHouseChimney} />} label="Fixed asset tax: 2%" />
            </div>
        </div>
    </div>
})