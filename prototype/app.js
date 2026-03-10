const dayNames = ["周一", "周二", "周三", "周四", "周五", "周六", "周日"];

const schedules = [
  {
    id: "me",
    name: "我的大三下",
    owner: "Lan",
    color: "#1f6feb",
    opacity: 0.86,
    visible: true,
    description: "主课表，偏满课日集中在周一到周四。",
    courses: [
      { day: 0, start: "08:00", end: "09:40", title: "编译原理", teacher: "赵老师", location: "信工楼 405" },
      { day: 1, start: "10:00", end: "11:40", title: "分布式系统", teacher: "Liu", location: "A-302" },
      { day: 2, start: "13:30", end: "15:10", title: "软件工程", teacher: "陈老师", location: "实验中心 201" },
      { day: 3, start: "15:30", end: "17:10", title: "机器学习", teacher: "Qin", location: "理科楼 511" },
      { day: 4, start: "09:00", end: "10:40", title: "项目研讨", teacher: "导师组", location: "创新基地" },
      { day: 4, start: "19:00", end: "20:30", title: "实验值班", teacher: "助教", location: "机房 3" }
    ]
  },
  {
    id: "april",
    name: "April 大二下",
    owner: "April",
    color: "#df5d43",
    opacity: 0.78,
    visible: true,
    description: "分享加入，语言与经管课程较多，方便约饭前看空档。",
    courses: [
      { day: 0, start: "10:00", end: "11:40", title: "国际贸易", teacher: "徐老师", location: "经管 202" },
      { day: 1, start: "08:30", end: "10:10", title: "英语演讲", teacher: "Ms. Sun", location: "外语楼 108" },
      { day: 2, start: "15:30", end: "17:10", title: "市场调研", teacher: "唐老师", location: "经管 307" },
      { day: 3, start: "13:00", end: "14:40", title: "统计学", teacher: "何老师", location: "经管 110" },
      { day: 5, start: "09:00", end: "11:40", title: "社团排班", teacher: "学生会", location: "学活 2F" }
    ]
  },
  {
    id: "kai",
    name: "Kai 考研作息",
    owner: "Kai",
    color: "#1d8b8f",
    opacity: 0.72,
    visible: false,
    description: "不是课表而是学习作息，用来比较共同空闲时段。",
    courses: [
      { day: 0, start: "07:30", end: "09:00", title: "高数晨读", teacher: "自习", location: "图书馆" },
      { day: 0, start: "14:00", end: "17:30", title: "专业课专注段", teacher: "自习", location: "图书馆" },
      { day: 2, start: "19:00", end: "21:00", title: "英语真题", teacher: "自习", location: "宿舍" },
      { day: 4, start: "14:00", end: "16:00", title: "政治复盘", teacher: "自习", location: "图书馆" },
      { day: 6, start: "09:00", end: "12:00", title: "周测模拟", teacher: "自习", location: "自习室" }
    ]
  }
];

const refs = {
  scheduleList: document.getElementById("schedule-list"),
  timeAxis: document.getElementById("time-axis"),
  timetable: document.getElementById("timetable"),
  legend: document.getElementById("legend"),
  statusGrid: document.getElementById("status-grid"),
  daySelect: document.getElementById("day-select"),
  timeSlider: document.getElementById("time-slider"),
  windowSlider: document.getElementById("window-slider"),
  currentReading: document.getElementById("current-reading"),
  scheduleCount: document.getElementById("schedule-count"),
  activeCount: document.getElementById("active-count"),
  windowValue: document.getElementById("window-value")
};

const STATUS_SECTIONS = [
  { key: "current", title: "正在上课" },
  { key: "free", title: "当前空闲" },
  { key: "soonStart", title: "即将上课" },
  { key: "soonEnd", title: "即将下课" }
];

function timeToMinutes(value) {
  const [hour, minute] = value.split(":").map(Number);
  return hour * 60 + minute;
}

function formatMinutes(totalMinutes) {
  const hour = Math.floor(totalMinutes / 60);
  const minute = totalMinutes % 60;
  return `${String(hour).padStart(2, "0")}:${String(minute).padStart(2, "0")}`;
}

function create(tag, className, text) {
  const el = document.createElement(tag);
  if (className) el.className = className;
  if (text) el.textContent = text;
  return el;
}

function renderDaySelect() {
  dayNames.forEach((name, index) => {
    const option = document.createElement("option");
    option.value = String(index);
    option.textContent = name;
    refs.daySelect.append(option);
  });

  const now = new Date();
  const weekday = (now.getDay() + 6) % 7;
  refs.daySelect.value = String(weekday);
}

function renderScheduleList() {
  refs.scheduleList.innerHTML = "";

  schedules.forEach((schedule) => {
    const item = create("article", "schedule-item");
    const head = create("div", "schedule-head");
    const title = create("div", "schedule-title");
    const swatch = create("span", "swatch");
    swatch.style.background = schedule.color;

    const heading = create("div");
    const name = create("h3", "", schedule.name);
    const owner = create("small", "", `所有者：${schedule.owner}`);
    heading.append(name, owner);
    title.append(swatch, heading);

    const link = create("small", "", `cw.io/share/${schedule.id}`);
    head.append(title, link);

    const desc = create("p", "", schedule.description);

    const controls = create("div", "schedule-controls");
    const toggle = create("label", "toggle");
    const checkbox = document.createElement("input");
    checkbox.type = "checkbox";
    checkbox.checked = schedule.visible;
    checkbox.addEventListener("change", () => {
      schedule.visible = checkbox.checked;
      renderAll();
    });
    toggle.append(checkbox, document.createTextNode("叠加显示"));

    const opacityRow = create("div", "opacity-row");
    const opacityLabel = create("label", "", "透明度");
    const opacityInput = document.createElement("input");
    opacityInput.type = "range";
    opacityInput.min = "35";
    opacityInput.max = "100";
    opacityInput.step = "1";
    opacityInput.value = String(Math.round(schedule.opacity * 100));

    const opacityOutput = create("output", "", `${opacityInput.value}%`);
    opacityInput.addEventListener("input", () => {
      schedule.opacity = Number(opacityInput.value) / 100;
      opacityOutput.textContent = `${opacityInput.value}%`;
      renderAll();
    });

    opacityRow.append(opacityLabel, opacityOutput);
    controls.append(toggle, opacityRow, opacityInput);
    item.append(head, desc, controls);
    refs.scheduleList.append(item);
  });
}

function renderTimeAxis() {
  refs.timeAxis.innerHTML = "";
  for (let hour = 6; hour <= 22; hour += 1) {
    const label = create("div", "axis-label", `${String(hour).padStart(2, "0")}:00`);
    const minutesFromStart = (hour - 6) * 60;
    label.style.top = `${(minutesFromStart / 960) * 100}%`;
    refs.timeAxis.append(label);
  }
}

function renderLegend() {
  refs.legend.innerHTML = "";
  schedules
    .filter((schedule) => schedule.visible)
    .forEach((schedule) => {
      const item = create("div", "legend-item");
      const swatch = create("span", "swatch");
      swatch.style.background = schedule.color;
      item.append(swatch, document.createTextNode(schedule.name));
      refs.legend.append(item);
    });
}

function renderTimetable() {
  refs.timetable.innerHTML = "";

  const currentDay = Number(refs.daySelect.value);
  const currentTime = Number(refs.timeSlider.value);

  dayNames.forEach((day, dayIndex) => {
    const column = create("section", "day-column");
    const header = create("div", "day-header");
    const strong = create("strong", "", day);
    const sub = create("span", "", dayIndex === currentDay ? "正在分析" : "周视图");
    header.append(strong, sub);

    const body = create("div", "day-body");

    schedules
      .filter((schedule) => schedule.visible)
      .forEach((schedule, visibleIndex) => {
        schedule.courses
          .filter((course) => course.day === dayIndex)
          .forEach((course) => {
            const start = timeToMinutes(course.start);
            const end = timeToMinutes(course.end);
            const block = create("article", "course-block");
            const top = ((start - 360) / 960) * 100;
            const height = ((end - start) / 960) * 100;
            block.style.top = `${top}%`;
            block.style.height = `${height}%`;
            block.style.background = schedule.color;
            block.style.opacity = String(schedule.opacity);
            block.style.left = `${10 + visibleIndex * 10}px`;
            block.style.right = `${10 + Math.max(0, (schedules.filter((item) => item.visible).length - visibleIndex - 1) * 10)}px`;

            if (dayIndex === currentDay && currentTime >= start && currentTime < end) {
              block.classList.add("current");
            }

            if (dayIndex !== currentDay) {
              block.classList.add("dimmed");
            }

            block.innerHTML = `
              <strong>${course.title}</strong>
              <span>${schedule.owner} / ${course.teacher}</span>
              <span>${course.location}</span>
              <span>${course.start} - ${course.end}</span>
            `;

            body.append(block);
          });
      });

    column.append(header, body);
    refs.timetable.append(column);
  });
}

function getStatusGroups() {
  const currentDay = Number(refs.daySelect.value);
  const currentTime = Number(refs.timeSlider.value);
  const windowMinutes = Number(refs.windowSlider.value);

  return schedules
    .filter((schedule) => schedule.visible)
    .reduce(
      (groups, schedule) => {
        const todaysCourses = schedule.courses
          .filter((course) => course.day === currentDay)
          .map((course) => ({
            ...course,
            startMinutes: timeToMinutes(course.start),
            endMinutes: timeToMinutes(course.end)
          }))
          .sort((a, b) => a.startMinutes - b.startMinutes);

        const currentCourse = todaysCourses.find(
          (course) => currentTime >= course.startMinutes && currentTime < course.endMinutes
        );
        const nextCourse = todaysCourses.find((course) => course.startMinutes > currentTime);

        if (currentCourse) {
          groups.current.push(`${schedule.owner} · ${currentCourse.title}`);
          if (currentCourse.endMinutes - currentTime <= windowMinutes) {
            groups.soonEnd.push(`${schedule.owner} · ${formatMinutes(currentCourse.endMinutes)} 下课`);
          }
        } else {
          groups.free.push(`${schedule.owner} · 当前空闲`);
        }

        if (nextCourse && nextCourse.startMinutes - currentTime <= windowMinutes) {
          groups.soonStart.push(`${schedule.owner} · ${formatMinutes(nextCourse.startMinutes)} 上课`);
        }

        return groups;
      },
      { current: [], free: [], soonStart: [], soonEnd: [] }
    );
}

function renderStatusGrid() {
  refs.statusGrid.innerHTML = "";
  const groups = getStatusGroups();

  STATUS_SECTIONS.forEach((section) => {
    const column = create("section", "status-column");
    const title = create("h3", "", section.title);
    const list = create("div", "pill-list");
    const items = groups[section.key];

    if (!items.length) {
      list.append(create("span", "status-empty", "暂无"));
    } else {
      items.forEach((item) => list.append(create("span", "pill", item)));
    }

    column.append(title, list);
    refs.statusGrid.append(column);
  });
}

function renderMetrics() {
  const visibleCount = schedules.filter((schedule) => schedule.visible).length;
  refs.scheduleCount.textContent = String(schedules.length);
  refs.activeCount.textContent = String(visibleCount);
  refs.windowValue.textContent = refs.windowSlider.value;
  refs.currentReading.textContent = `${dayNames[Number(refs.daySelect.value)]} ${formatMinutes(Number(refs.timeSlider.value))}`;
}

function renderAll() {
  renderLegend();
  renderTimetable();
  renderStatusGrid();
  renderMetrics();
}

function init() {
  renderDaySelect();
  renderScheduleList();
  renderTimeAxis();

  const now = new Date();
  const currentMinutes = now.getHours() * 60 + now.getMinutes();
  const snappedMinutes = Math.min(1320, Math.max(360, Math.round(currentMinutes / 15) * 15));
  refs.timeSlider.value = String(snappedMinutes);

  refs.daySelect.addEventListener("change", renderAll);
  refs.timeSlider.addEventListener("input", renderAll);
  refs.windowSlider.addEventListener("input", renderAll);

  renderAll();
}

init();
