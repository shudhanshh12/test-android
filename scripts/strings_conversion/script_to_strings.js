var fs  = require("fs");

fs.readFileSync('./strings.tsv').toString().split('\n').forEach(function (line, index) {
    const tabs = line.split('\t');
    if (!fs.existsSync('output') && index == 0) {
      fs.mkdirSync('output');
    }

    if (index != 0) {
      save(tabs[0], tabs[2], index - 1, '../../resources/src/main/res/values');
      save(tabs[0], tabs[3], index - 1, '../../resources/src/main/res/values-hi');
      save(tabs[0], tabs[4], index - 1, '../../resources/src/main/res/values-afh');
      save(tabs[0], tabs[5], index - 1, '../../resources/src/main/res/values-pa');
      save(tabs[0], tabs[6], index - 1, '../../resources/src/main/res/values-ml');
      save(tabs[0], tabs[7], index - 1, '../../resources/src/main/res/values-mr');
      save(tabs[0], tabs[8], index - 1, '../../resources/src/main/res/values-gu');
      save(tabs[0], tabs[9], index - 1, '../../resources/src/main/res/values-te');
      save(tabs[0], tabs[10], index - 1, '../../resources/src/main/res/values-ta');
    }
});

function save(id, content, index, dir) {
  if (!fs.existsSync(dir) && index == 0) {
      fs.mkdirSync(dir);
  }
  if (index == 0 && fs.existsSync(`${dir}/strings.xml`)) {
    fs.unlinkSync(`${dir}/strings.xml`);
    fs.appendFileSync(`${dir}/strings.xml`, `<?xml version="1.0" encoding="utf-8"?>\n<resources>\n`);

    // Adding </resources> after 1 sec
    setTimeout(function () {
        fs.appendFileSync(`${dir}/strings.xml`, `</resources>`);
    }, 1000)
  }
  fs.appendFileSync(`${dir}/strings.xml`, `    <string name="${id}">${content}</string>\n`);
}